from fastapi import FastAPI
from fastapi.responses import StreamingResponse
from fastapi.middleware.cors import CORSMiddleware
import cv2
from ai_module import VirtualTryOnAI # Import class vừa tạo ở trên


app = FastAPI()

@app.get("/")
def index():
    return {"message": "Server AI đang chạy! Hãy vào /video_feed để xem camera."}

# Cho phép FE gọi vào
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_methods=["*"],
    allow_headers=["*"],
)

ai_engine = VirtualTryOnAI()
camera = cv2.VideoCapture(0) 
camera.set(3, 1280)
camera.set(4, 720)

def generate_frames():
    while True:
        success, frame = camera.read()
        if not success:
            break # Hoặc đọc lại video nếu là file
            
        processed_frame = ai_engine.process_frame(frame)
        ret, buffer = cv2.imencode('.jpg', processed_frame)
        frame_bytes = buffer.tobytes()
        yield (b'--frame\r\n'
               b'Content-Type: image/jpeg\r\n\r\n' + frame_bytes + b'\r\n')

@app.get("/video_feed")
def video_feed():
    return StreamingResponse(generate_frames(), media_type="multipart/x-mixed-replace;boundary=frame")

if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=5001)