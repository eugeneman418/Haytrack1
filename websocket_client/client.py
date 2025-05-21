import asyncio
import websockets
import numpy as np
import cv2

async def stream_images():
    uri = "ws://192.168.80.84:8887"  # Replace with your Android server IP and port

    async with websockets.connect(uri, max_size=None) as websocket:
        print("Connected to the WebSocket server.")

        while True:
            message = await websocket.recv()

            if isinstance(message, bytes):
                # Convert byte array (JPEG) to numpy array
                nparr = np.frombuffer(message, np.uint8)
                img = cv2.imdecode(nparr, cv2.IMREAD_COLOR)

                if img is not None:
                    cv2.imshow("Android Camera Stream", img)
                    if cv2.waitKey(1) & 0xFF == ord('q'):
                        break
                else:
                    print("Failed to decode image.")
            else:
                print("Received text message:", message)

    cv2.destroyAllWindows()

if __name__ == "__main__":
    asyncio.run(stream_images())
