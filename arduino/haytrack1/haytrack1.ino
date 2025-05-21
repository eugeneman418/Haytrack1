#include <Servo.h>
#include <SerialBT.h>

#define OK 1
#define ERR 2
#define TOUT 3
#define TIMEOUT 50  // Timeout in milliseconds

#define HORIZONAL_SERVO_PIN 18
#define VERTICAL_SERVO_PIN 21

#define PULSE_MIN 500
#define PULSE_MAX 2500

#define TIMEOUT 50

Servo horizontal;
Servo vertical;

void setup() {
  
  Serial.begin(9600);
  SerialBT.setName("Haytrack");
  SerialBT.begin();
  delay(1000);

  horizontal.attach(HORIZONAL_SERVO_PIN, PULSE_MIN, PULSE_MAX);
  vertical.attach(VERTICAL_SERVO_PIN, PULSE_MIN, PULSE_MAX);
  
  Serial.println("Setup complete. Waiting for Bluetooth data...");
}

uint8_t decodePosition(char encodedChar) {
    if (encodedChar < 0x20 || encodedChar > 0x6F) {
        // Optional: handle error, maybe return an invalid value like 255 or use assertion
        Serial.println("Error: Encoded character out of range.");
        return 255; // or some other sentinel value
    }
    return (uint8_t)(encodedChar - 0x20);
}

void updateServo(uint8_t horizontal_pos, uint8_t vertical_pos) {
  if (horizontal_pos >= 0 && horizontal_pos < 80) {
    int hpos = map(horizontal_pos, 0, 79, PULSE_MIN, PULSE_MAX);
    horizontal.writeMicroseconds(hpos);
  }
  if (vertical_pos >= 0 && vertical_pos < 80) {
    int vpos = map(vertical_pos, 0, 79, PULSE_MIN, PULSE_MAX);
    vertical.writeMicroseconds(vpos);
  }
}

uint8_t servo_buffer[2];
int idx = 0;
void loop() {
  while (SerialBT.available()) {
    servo_buffer[idx] = decodePosition(SerialBT.read());
    idx += 1;
    if (idx == 2) { // update servo
      idx = 0;
      updateServo(servo_buffer[0], servo_buffer[1]);
      delay(TIMEOUT);
    }
  }
}
