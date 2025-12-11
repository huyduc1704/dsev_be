import cv2
import tempfile
import os
import cvzone
from cvzone.PoseModule import PoseDetector

#Set a custom temporary directory for cv2
os.environ['TMPDIR'] = '/Users/user/Downloads/ShirtsTryOn/tmp'

# Initialize video capture
cap = cv2.VideoCapture(0)

# Initialize pose detector
detector = PoseDetector(staticMode=False,
                        modelComplexity=1,
                        smoothLandmarks=True,
                        enableSegmentation=False,
                        smoothSegmentation=True,
                        detectionCon=0.7,
                        trackCon=0.7)

shirtFolderPath = "Resources/Shirts"
listShirts = os.listdir(shirtFolderPath)
#print(listShirts)
fixedRatio = 262/190  #widthOfShirt/withOfPoint11to12

while True:
    success, img = cap.read()
    img = detector.findPose(img)
    img = cv2.flip(img, 1)  # Flip the image horizontally for a mirror effect
    lmList, bboxInfo = detector.findPosition(img, bboxWithHands=False, draw=False)
    if lmList:
        #center = bboxInfo["center"]
        lm11 = lmList[11][1:3]
        lm12 = lmList[12][1:3]
        imgShirt = cv2.imread(os.path.join(shirtFolderPath, listShirts[0]), cv2.IMREAD_UNCHANGED)
        imgShirt = cv2.resize(imgShirt, (0,0), None, 0.5, 0.5)

        withOfShirt = int((lm12[0]-lm11[0])*fixedRatio)
        print

        try:
            img = cvzone.overlayPNG(img, imgShirt, lm12)
        except:
            pass

    cv2.imshow("Image", img)
    if cv2.waitKey(1) & 0xFF == ord('q'):  # Press 'q' to quit
        break

cap.release()
cv2.destroyAllWindows()