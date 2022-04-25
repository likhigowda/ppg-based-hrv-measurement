import numpy as np
import cv2
import base64

# img[row,col] gives BGR value
# img.shape[0]
# [on_true] if [expression] else [on_false] -- (320, 240) (256, 144) (128, 96)

def get_red(img):
    length = 320 if img.shape[0] > 320 else img.shape[0]
    width = 240 if img.shape[1] > 240 else img.shape[1]
    r = 0;
    count = 0;
    for row in range(length):
        for col in range(width):
            r = r + img[row,col,2]
            count = count + 1

    return round(r/count)


def main(data):
    decoded_data = base64.b64decode(data)
    np_data = np.fromstring(decoded_data,np.uint8)
    img = cv2.imdecode(np_data,cv2.IMREAD_UNCHANGED)
    value = get_red(img)

    return str(value)












