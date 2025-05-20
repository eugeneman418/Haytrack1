import asyncio
import websockets

async def connect():
    # Replace with your Android device's IP address
    server_ip = "192.168.2.1"
    port = 8887
    uri = f"ws://{server_ip}:{port}"

    try:
        async with websockets.connect(uri) as websocket:
            print("Connected to server")

            await websocket.send("Hello from Python client!")
            print("Sent: Hello from Python client!")

            # Wait for a response (e.g., "Hello World" from server)
            response = await websocket.recv()
            print(f"Received from server: {response}")

    except Exception as e:
        print(f"Connection failed: {e}")

if __name__ == "__main__":
    asyncio.run(connect())
