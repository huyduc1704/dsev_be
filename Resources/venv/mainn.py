import os
import cv2
import tkinter as tk
from tkinter import ttk
from cvzone.PoseModule import PoseDetector
import cvzone

# Function to start camera feed with selected shirt images based on gender selection
def start_camera(gender):
    root.withdraw()
    cap = cv2.VideoCapture(0)
    cap.set(cv2.CAP_PROP_FRAME_WIDTH, 1280)
    cap.set(cv2.CAP_PROP_FRAME_HEIGHT, 720)

    detector = PoseDetector(staticMode=False,
                            modelComplexity=1,
                            smoothLandmarks=True,
                            enableSegmentation=False,
                            smoothSegmentation=True,
                            detectionCon=0.7,
                            trackCon=0.7)

    # --- SETUP TÀI NGUYÊN ---
    shirtFolderPath = "Resources/Shirts"
    buttonPrevPath = "Resources/Buttons/button-previous.png"
    buttonNextPath = "Resources/Buttons/button-next.png"

    if not os.path.exists(shirtFolderPath) or not os.path.exists(buttonPrevPath) or not os.path.exists(buttonNextPath):
        print("Lỗi: Không tìm thấy thư mục 'Resources/Shirts' hoặc các file button trong 'Resources/Buttons/'")
        return

    imgButtonPrev = cv2.imread(buttonPrevPath, cv2.IMREAD_UNCHANGED)
    imgButtonNext = cv2.imread(buttonNextPath, cv2.IMREAD_UNCHANGED)
    btn_h, btn_w, _ = imgButtonPrev.shape
    
    buttonPrevPos = (70, 285)
    buttonNextPos = (1080, 285)

    listShirts = os.listdir(shirtFolderPath)
    listShirts = [shirt for shirt in listShirts if not shirt.startswith('.')]

    if gender == "Male":
        selected_shirts_indices = [0, 1]
    else:
        selected_shirts_indices = [2, 3]

    fixedRatio = 262 / 190
    shirtRatioHeightWidth = 581 / 440
    imageNumber = 0
    
    counterLeft = 0
    counterRight = 0
    selectionSpeed = 10 

    def stop_camera():
        cap.release()
        cv2.destroyAllWindows()
        root.deiconify()

    while True:
        success, img = cap.read()
        if not success:
            break
        img = cv2.flip(img, 1)

        img = cvzone.overlayPNG(img, imgButtonPrev, buttonPrevPos)
        img = cvzone.overlayPNG(img, imgButtonNext, buttonNextPos)

        img = detector.findPose(img, draw=False)
        lmList, bboxInfo = detector.findPosition(img, bboxWithHands=False, draw=False)

        if lmList:
            # --- PHẦN LOGIC ÁO (Giữ nguyên) ---
            lm11 = lmList[11][0:2]
            lm12 = lmList[12][0:2]
            shirt_filename = listShirts[selected_shirts_indices[imageNumber]]
            imgShirt = cv2.imread(os.path.join(shirtFolderPath, shirt_filename), cv2.IMREAD_UNCHANGED)
            widthOfShoulders = abs(lm11[0] - lm12[0])
            widthOfShirt = int(widthOfShoulders * fixedRatio)
            heightOfShirt = int(widthOfShirt * shirtRatioHeightWidth)

            if widthOfShirt > 0 and heightOfShirt > 0:
                imgShirt = cv2.resize(imgShirt, (widthOfShirt, heightOfShirt))
                currentScale = widthOfShoulders / 190
                offset = int(44 * currentScale), int(48 * currentScale)
                shirt_pos_x = lm12[0] - offset[0]
                shirt_pos_y = lm12[1] - offset[1]
                try:
                    img = cvzone.overlayPNG(img, imgShirt, (shirt_pos_x, shirt_pos_y))
                except Exception as e:
                    pass
            
            # --- LOGIC TƯƠNG TÁC ĐÃ SỬA LỖI ---
            # Sau khi lật ảnh:
            # - Tay bên trái màn hình là landmark #20 (RIGHT_INDEX)
            # - Tay bên phải màn hình là landmark #19 (LEFT_INDEX)
            finger_tip_on_left_side = lmList[20][0:2]
            finger_tip_on_right_side = lmList[19][0:2]

            # 1. Kiểm tra nút PREVIOUS (bên trái) với tay bên trái (landmark #20)
            if buttonPrevPos[0] < finger_tip_on_left_side[0] < buttonPrevPos[0] + btn_w and \
               buttonPrevPos[1] < finger_tip_on_left_side[1] < buttonPrevPos[1] + btn_h:
                counterLeft += 1
                cv2.ellipse(img, (buttonPrevPos[0] + btn_w // 2, buttonPrevPos[1] + btn_h // 2), (btn_w // 2, btn_h // 2), 0, 0,
                            counterLeft * selectionSpeed, (0, 255, 0), 15)
                if counterLeft * selectionSpeed > 360:
                    counterLeft = 0
                    if imageNumber > 0:
                        imageNumber -= 1
            else:
                counterLeft = 0

            # 2. Kiểm tra nút NEXT (bên phải) với tay bên phải (landmark #19)
            if buttonNextPos[0] < finger_tip_on_right_side[0] < buttonNextPos[0] + btn_w and \
                 buttonNextPos[1] < finger_tip_on_right_side[1] < buttonNextPos[1] + btn_h:
                counterRight += 1
                cv2.ellipse(img, (buttonNextPos[0] + btn_w // 2, buttonNextPos[1] + btn_h // 2), (btn_w // 2, btn_h // 2), 0, 0,
                            counterRight * selectionSpeed, (0, 255, 0), 15)
                if counterRight * selectionSpeed > 360:
                    counterRight = 0
                    if imageNumber < len(selected_shirts_indices) - 1:
                        imageNumber += 1
            else:
                counterRight = 0

        cv2.imshow("FitMe360", img)
        key = cv2.waitKey(1)
        if key == ord('q'):
            stop_camera()
            break

# --- GIAO DIỆN TKINTER (Không thay đổi) ---
root = tk.Tk()
root.title("FitMe360")
root.geometry("600x400")
root.configure(bg="#000000")

def select_gender():
    gender = gender_var.get()
    if gender:
        start_camera(gender)

label_banner = ttk.Label(root, text="FitMe360", background="#000000", foreground="white", font=("Now", 24, "bold"))
label_banner.pack(side="top", pady=10)
label_gender = ttk.Label(root, text="Select your gender:", background="#000000", foreground="white", font=("Helvetica", 14))
label_gender.pack(pady=(20, 5), padx=10)
gender_var = tk.StringVar()
gender_combobox = ttk.Combobox(root, textvariable=gender_var, values=["Male", "Female"], font=("Helvetica", 12), state="readonly")
gender_combobox.pack(pady=5, padx=10)
gender_combobox.set("Male")
start_button = ttk.Button(root, text="Start", command=select_gender, style="StartButton.TButton")
start_button.place(relx=0.5, rely=0.5, anchor=tk.CENTER)
style = ttk.Style(root)
style.configure("StartButton.TButton", background="black", foreground="black", font=("Helvetica", 12), padding=10)

root.mainloop()