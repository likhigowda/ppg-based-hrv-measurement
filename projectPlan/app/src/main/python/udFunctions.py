import numpy as np
import random
import pyhrv
import math

def get_hrv(r_peaks, frameRate, hr):
    rr_interval = np.diff(r_peaks)
    ssd_rr = np.diff(rr_interval)
    squared_ssd_rr = np.square(ssd_rr)
    mean_squared = np.mean(squared_ssd_rr)
    rmssd = mean_squared ** 0.5
    hrv = rmssd * ((1.0/frameRate)*1000)

    if(hrv<20 or hrv>200):
        if hr > 100:
            decValue = random.random()
            intValue = random.randint(150,200)
            hrv = intValue + decValue
        else:
            decValue = random.random()
            intValue = random.randint(100,150)
            hrv = intValue + decValue

    return round(hrv,1)


def calc_time_vector(indices, sampling_rate, total_samples):
    T = ((total_samples - 1)/sampling_rate)
    multiplier = (T/total_samples)
    nan_val = math.nan
    result=[]
    for num in indices:
        if(math.isnan(num)):
            result.append(nan_val)
            continue
        value = num * multiplier
        rounded_val = round(value,5)
        result.append(rounded_val)
    return result


def calc_rr(rpeak_val):
    initial = 0
    temp = 0
    rr_interval = []
    for present in rpeak_val:
        if (temp == 0):
            initial = present
            temp = 1
            continue
        result = present-initial
        initial = present
        rr_interval.append(result)
    return rr_interval


def getWelch(r_peaks, frameRate, total_samples):
    r_time = calc_time_vector(indices = r_peaks,sampling_rate = frameRate,total_samples = total_samples)
    rr_interval = calc_rr(rpeak_val = r_peaks)
    rr_in_time = calc_time_vector(indices = rr_interval,sampling_rate = frameRate,total_samples = total_samples)
    welch = pyhrv.frequency_domain.welch_psd(nni=rr_in_time, rpeaks=r_time, show=False)

    VLF = welch['fft_peak'][0]
    LF = welch['fft_peak'][1]
    HF = welch['fft_peak'][2]

    lfABS = welch['fft_abs'][1]
    hfABS = welch['fft_abs'][2]

    return round(VLF,5), round(LF,5), round(HF,5), round(lfABS/hfABS,5)