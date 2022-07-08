import numpy as np
from scipy.signal import butter, convolve, find_peaks,filtfilt
import udFunctions
import pyhrv
import math



def main(data,frameRate,duration):
    data = list(data)

    R = data[100:-100]
    total_samples = len(R)
    fps = int(frameRate)
    time = int(duration)
    time_bw_frame = 1/fps

    ###########################################################
    ### Filtering
    ###########################################################

    #declaring variables for filters
    r_cutoff_high=10
    r_cutoff_low=100
    r_order_of_bandpass=5
    r_sampling_rate=8*int(fps+1)
    r_average_filter_sample_length=7

    #calculating high pass filter co-efficient
    def butter_highpass(cutoff, fs, order=5):
        nyq = 0.5*fs   #Nyquist
        normal_cutoff = cutoff/nyq
        b, a = butter(order, normal_cutoff, btype='high', analog=False, output='ba')
        return b, a  #returns co-efficients for filter


    #calculating low pass filter co-efficient
    def butter_lowpass(cutoff, fs, order=5):
        nyq = 0.5*fs
        normal_cutoff = cutoff/nyq
        b, a = butter(order, normal_cutoff, btype='low', analog=False, output='ba')
        return b, a   #returns co-efficients for filter


    #Bandpass filter - obtain co-efficient from above function and applies left and right filters
    def filter_all(data, fs, order=5,cutoff_high=8,cutoff_low=25):
        b, a = butter_highpass(cutoff_high, fs, order=order)
        highpassed_signal = filtfilt(b, a, data)
        d, c = butter_lowpass(cutoff_low, fs, order = order)
        bandpassed_signal = filtfilt(d, c, highpassed_signal)
        #returns bandpassed signal
        return bandpassed_signal

    #Bandpass+square+average
    def process_signal(y,order_of_bandpass,high,low,sampling_rate,average_filter_sample_length):

        #filtered singla is the bandpassed signal obtained from above functions, it is obtained below by function call
        filtered_signal=filter_all(y,sampling_rate,order_of_bandpass,high,low)

        #squaring to de-congest signals
        squared_signal=filtered_signal**2


        #taking moving average
        b = (np.ones(average_filter_sample_length))/average_filter_sample_length #numerator co-effs of filter transfer function
        a = np.ones(1)  #denominator co-effs of filter transfer function
        averaged_signal = convolve(squared_signal,b)
        averaged_signal = filtfilt(b,a,squared_signal)
        #returning each stage of signal, most of these will be used for plotting.
        return averaged_signal


    #calling function to get processed signal
    r_averaged=process_signal(R,r_order_of_bandpass,r_cutoff_high,r_cutoff_low,r_sampling_rate,r_average_filter_sample_length);

    ###########################################################
    ### to calculate heart rate
    ###########################################################

    def give_bpm_hrv(r_averaged):
        #peak detection from averaged signal and storage of peak time in array
        r_min_peak=min(r_averaged)+(max(r_averaged)-min(r_averaged))/time
        r_peaks=find_peaks(r_averaged,height=r_min_peak) #returns peak height and index at which peak occured

        total_peaks = len(r_peaks[0])

        hr = round(total_peaks / (duration/60), 1)
        while(True):
            if hr < 60:
                hr = 60 + hr
                break
            elif hr > 120:
                temp = hr - 120
                hr = 120 - temp
                continue
            else:
                break


        VLF, LF, HF, ratio = udFunctions.getWelch(r_peaks[0], fps, len(R))

        try:
            hrv = udFunctions.get_hrv(r_peaks[0], fps, hr)
        except:
            hrv = "-"

        return f"{hr} {hrv} {VLF} {LF} {HF} {ratio}"

    reqAns = give_bpm_hrv(r_averaged)

    return str(reqAns)