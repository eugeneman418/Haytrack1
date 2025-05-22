import asyncio
import websockets
import numpy as np
import cv2
import msgpack
import threading
import time
from tkinter import Tk, Label, Button, Frame
from PIL import Image, ImageTk

# ==== GLOBAL CONFIGURATIONS ====
MOVE_SPEED = -1
CONTROL_INTERVAL = 0.1

START_STREAMING = "START_STREAMING"
STOP_STREAMING = "STOP_STREAMING"
START_RECORDING = "START_RECORDING"
STOP_RECORDING = "STOP_RECORDING"
START_MANUAL = "MANUAL_CONTROL"
STOP_MANUAL = "AI_TAKE_THE_WHEEL"
# ===============================

class WebSocketClient:
    def __init__(self, uri, gui):
        self.uri = uri
        self.gui = gui
        self.ws = None
        self.running = False
        self.key_state = {'w': False, 'a': False, 's': False, 'd': False}

    async def connect(self):
        async with websockets.connect(self.uri, max_size=None) as websocket:
            self.ws = websocket
            self.running = True
            consumer_task = asyncio.create_task(self.receive_images())
            producer_task = asyncio.create_task(self.send_controls())
            await asyncio.gather(consumer_task, producer_task)

    async def receive_images(self):
        last_update = 0
        target_interval = 1 / 60  # 60 FPS

        while self.running:
            try:
                data = await self.ws.recv()
                now = time.time()
                if now - last_update < target_interval:
                    continue
                last_update = now

                image_np = np.frombuffer(data, dtype=np.uint8)
                frame = cv2.imdecode(image_np, cv2.IMREAD_COLOR)
                if frame is not None:
                    self.gui.update_image(frame)
                else:
                    print("Received invalid frame.")
            except Exception as e:
                print(f"Error receiving image: {e}")
                break

    async def send_controls(self):
        while self.running:
            h = (self.key_state['d'] - self.key_state['a']) * MOVE_SPEED
            v = (self.key_state['s'] - self.key_state['w']) * MOVE_SPEED
            packed = msgpack.packb([int(h), int(v)])
            try:
                await self.ws.send(packed)
            except Exception as e:
                print(f"Error sending control: {e}")
                break
            await asyncio.sleep(CONTROL_INTERVAL)

    def set_key_state(self, key, pressed):
        if key in self.key_state:
            self.key_state[key] = pressed

    async def send_command(self, command_str):
        if self.ws:
            try:
                await self.ws.send(command_str)
            except Exception as e:
                print(f"Failed to send command: {e}")

    def stop(self):
        self.running = False


class ClientGUI:
    def __init__(self, root, ws_client):
        self.root = root
        self.ws_client = ws_client

        # Use Label instead of Canvas to reduce flicker
        self.label = Label(root)
        self.label.pack()

        self.current_image = None

        # Button frame
        button_frame = Frame(root)
        button_frame.pack(pady=10)

        self.btn_start_stream = Button(button_frame, text="Start Stream", command=lambda: self.send_command(START_STREAMING))
        self.btn_stop_stream = Button(button_frame, text="Stop Stream", command=lambda: self.send_command(STOP_STREAMING))
        self.btn_start_record = Button(button_frame, text="Start Recording", command=lambda: self.send_command(START_RECORDING))
        self.btn_stop_record = Button(button_frame, text="Stop Recording", command=lambda: self.send_command(STOP_RECORDING))
        self.btn_manual = Button(button_frame, text="Manual Control", command=lambda: self.send_command(START_MANUAL))
        self.btn_auto = Button(button_frame, text="Auto Control", command=lambda: self.send_command(STOP_MANUAL))

        self.btn_start_stream.grid(row=0, column=0, padx=5, pady=5)
        self.btn_stop_stream.grid(row=0, column=1, padx=5, pady=5)
        self.btn_start_record.grid(row=1, column=0, padx=5, pady=5)
        self.btn_stop_record.grid(row=1, column=1, padx=5, pady=5)
        self.btn_manual.grid(row=2, column=0, padx=5, pady=5)
        self.btn_auto.grid(row=2, column=1, padx=5, pady=5)

        self.root.bind("<KeyPress>", self.on_key_press)
        self.root.bind("<KeyRelease>", self.on_key_release)

    def update_image(self, frame):
        img = cv2.cvtColor(frame, cv2.COLOR_BGR2RGB)
        img = Image.fromarray(img)
        new_image = ImageTk.PhotoImage(image=img)

        def update():
            self.current_image = new_image  # keep reference to avoid GC
            self.label.config(image=self.current_image)

        self.root.after(0, update)

    def on_key_press(self, event):
        self.ws_client.set_key_state(event.keysym.lower(), True)

    def on_key_release(self, event):
        self.ws_client.set_key_state(event.keysym.lower(), False)

    def send_command(self, value):
        asyncio.run_coroutine_threadsafe(
            self.ws_client.send_command(value),
            asyncio.get_event_loop()
        )


def start_client():
    uri = "ws://192.168.2.7:8887"
    root = Tk()
    root.title("WebSocket Video Client")

    gui = ClientGUI(root, None)
    client = WebSocketClient(uri, gui)
    gui.ws_client = client

    loop = asyncio.new_event_loop()
    asyncio.set_event_loop(loop)

    def run_loop():
        loop.run_until_complete(client.connect())

    threading.Thread(target=run_loop, daemon=True).start()
    root.mainloop()
    client.stop()


if __name__ == "__main__":
    start_client()
