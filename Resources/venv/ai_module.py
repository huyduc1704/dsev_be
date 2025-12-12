import cv2
import cvzone
from cvzone.PoseModule import PoseDetector
import os

class VirtualTryOnAI:
    def __init__(self):
        # Khởi tạo camera và model
        # Lưu ý: Khi chạy server, nên để camera index là 0 hoặc đường dẫn video test
        self.detector = PoseDetector(staticMode=False, modelComplexity=1, smoothLandmarks=True,
                                     enableSegmentation=False, smoothSegmentation=True,
                                     detectionCon=0.7, trackCon=0.7)
        
        # Đường dẫn tài nguyên (Cần đảm bảo đường dẫn đúng khi chạy server)
        self.shirtFolderPath = "Resources/Shirts"  
        self.buttonPrevPath = "Resources/Buttons/button-previous.png"
        self.buttonNextPath = "Resources/Buttons/button-next.png"
        
        self.listShirts = sorted(os.listdir(self.shirtFolderPath))
        self.imgButtonPrev = cv2.imread(self.buttonPrevPath, cv2.IMREAD_UNCHANGED)
        self.imgButtonNext = cv2.imread(self.buttonNextPath, cv2.IMREAD_UNCHANGED)
        
        # Validate ảnh
        if self.imgButtonPrev is None or self.imgButtonNext is None:
            raise ValueError("Không tìm thấy ảnh nút bấm! Kiểm tra lại đường dẫn Resources.")

        self.btn_h, self.btn_w, _ = self.imgButtonPrev.shape
        self.buttonPrevPos = (50, 250) # Vị trí nút trái
        self.buttonNextPos = (1000, 250) # Vị trí nút phải (chỉnh cho vừa khung hình web)

        self.current_shirt_index = 0
        self.counterLeft = 0
        self.counterRight = 0
        self.selectionSpeed = 8 # Tốc độ loading nút

    def process_frame(self, img):
        # 1. Lật ảnh gương
        img = cv2.flip(img, 1)

        # 2. Vẽ nút bấm lên hình (FE không cần vẽ, AI vẽ luôn vào video)
        img = cvzone.overlayPNG(img, self.imgButtonPrev, self.buttonPrevPos)
        img = cvzone.overlayPNG(img, self.imgButtonNext, self.buttonNextPos)

        # 3. Detect người
        img = self.detector.findPose(img, draw=False)
        lmList, bboxInfo = self.detector.findPosition(img, bboxWithHands=False, draw=False)

        if lmList:
            # --- Logic Mặc Áo ---
            lm11 = lmList[11][0:2]
            lm12 = lmList[12][0:2]
            
            widthOfShoulders = abs(lm11[0] - lm12[0])
            
            # Chỉ mặc áo khi phát hiện vai đủ lớn (người đứng gần)
            if widthOfShoulders > 20:
                fixedRatio = 262 / 190
                shirtRatioHeightWidth = 581 / 440
                
                imgShirt = cv2.imread(os.path.join(self.shirtFolderPath, self.listShirts[self.current_shirt_index]), cv2.IMREAD_UNCHANGED)
                
                widthOfShirt = int(widthOfShoulders * fixedRatio)
                heightOfShirt = int(widthOfShirt * shirtRatioHeightWidth)

                try:
                    imgShirt = cv2.resize(imgShirt, (widthOfShirt, heightOfShirt))
                    currentScale = widthOfShoulders / 190
                    offset = int(44 * currentScale), int(48 * currentScale)
                    
                    shirt_pos_x = lm12[0] - offset[0]
                    shirt_pos_y = lm12[1] - offset[1]
                    
                    img = cvzone.overlayPNG(img, imgShirt, (shirt_pos_x, shirt_pos_y))
                except:
                    pass

            # --- Logic Chạm Nút (Gesture Control) ---
            # Tay trái (Landmark 20), Tay phải (Landmark 19)
            if len(lmList) > 20:
                hand_left = lmList[20][0:2]
                hand_right = lmList[19][0:2]

                # Check nút Prev
                if self.check_click(hand_left, self.buttonPrevPos):
                    self.counterLeft += 1
                    cv2.ellipse(img, (self.buttonPrevPos[0] + self.btn_w//2, self.buttonPrevPos[1] + self.btn_h//2),
                                (self.btn_w//2, self.btn_h//2), 0, 0, self.counterLeft * self.selectionSpeed, (0, 255, 0), 10)
                    if self.counterLeft * self.selectionSpeed > 360:
                        self.counterLeft = 0
                        self.current_shirt_index = (self.current_shirt_index - 1) % len(self.listShirts)
                else:
                    self.counterLeft = 0

                # Check nút Next
                if self.check_click(hand_right, self.buttonNextPos):
                    self.counterRight += 1
                    cv2.ellipse(img, (self.buttonNextPos[0] + self.btn_w//2, self.buttonNextPos[1] + self.btn_h//2),
                                (self.btn_w//2, self.btn_h//2), 0, 0, self.counterRight * self.selectionSpeed, (0, 255, 0), 10)
                    if self.counterRight * self.selectionSpeed > 360:
                        self.counterRight = 0
                        self.current_shirt_index = (self.current_shirt_index + 1) % len(self.listShirts)
                else:
                    self.counterRight = 0

        return img

    def check_click(self, hand_pos, btn_pos):
        x, y = hand_pos
        bx, by = btn_pos
        return bx < x < bx + self.btn_w and by < y < by + self.btn_h