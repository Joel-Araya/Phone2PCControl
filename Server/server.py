import socket
import threading
import json
import time
from pynput.mouse import Controller as MouseController, Button
from pynput.keyboard import Controller as KeyboardController, Key

HOST = '0.0.0.0'
PORT = 5050
DISCOVERY_PORT = 5051  # Puerto para discovery UDP
FACTOR = 5  # Ajusta la velocidad de movimiento aquí
FACTOR_SCROLL = 10  # Factor de scroll, puedes ajustarlo si es necesario

mouse = MouseController()
keyboard = KeyboardController()

# Map para botones de mouse
MOUSE_BUTTONS = {
    "left": Button.left,
    "right": Button.right,
    "middle": Button.middle,
}

# Map para teclas especiales
KEYS = {
    "ctrl": Key.ctrl,
    "alt": Key.alt,
    "altgr": Key.alt_r,
    "shift": Key.shift,
    "enter": Key.enter,
    "esc": Key.esc,
    "tab": Key.tab,
    "backspace": Key.backspace,
    "space": Key.space,
    "up": Key.up,
    "down": Key.down,
    "left": Key.left,
    "right": Key.right,
    "win": Key.cmd if hasattr(Key, "cmd") else Key.cmd_l,
}

ONE_SHOT_KEYS = {Key.esc, Key.enter, Key.tab, Key.backspace, Key.space, Key.up, Key.down, Key.left, Key.right}
pressed_keys = set()  # Mantiene las teclas especiales actualmente presionadas

class MouseMover:
    def __init__(self, factor=1):
        self.dx = 0.0
        self.dy = 0.0
        self.lock = threading.Lock()
        self.factor = factor
        self.running = True
        self.thread = threading.Thread(target=self.worker, daemon=True)
        self.thread.start()

    def add_movement(self, dx, dy):
        with self.lock:
            self.dx += dx
            self.dy += dy

    def worker(self):
        while self.running:
            time.sleep(0.015)
            with self.lock:
                if self.dx != 0 or self.dy != 0:
                    mouse.move(self.dx * self.factor, self.dy * self.factor)
                    self.dx = 0.0
                    self.dy = 0.0

    def stop(self):
        self.running = False
        self.thread.join()

def handle_client(conn, addr, mover: MouseMover):
    print(f"Conexión establecida desde: {addr}")
    buffer = ""

    try:
        while True:
            data = conn.recv(1024)
            if not data:
                break
            buffer += data.decode()

            while '\n' in buffer:
                line, buffer = buffer.split('\n', 1)
                line = line.strip()
                if not line:
                    continue

                try:
                    msg = json.loads(line)
                    tipo = msg.get("type")
                    print(f"Mensaje recibido: {msg}")

                    if tipo == "move":
                        dx = float(msg.get("dx", 0))
                        dy = float(msg.get("dy", 0))
                        mover.add_movement(dx, dy)

                    elif tipo == "scroll":
                        dy = float(msg.get("dy", 0)) * FACTOR_SCROLL
                        mouse.scroll(0, int(dy))
                        print(f"Scroll: {dy}")

                    elif tipo == "click":
                        button_str = msg.get("key", "").lower()
                        if button_str in MOUSE_BUTTONS:
                            button = MOUSE_BUTTONS[button_str]
                            action = msg.get("action", "").lower()
                            print(f"Acción del botón: {action}, válida: {action in ['click', 'arrastre']}")
                            if action == "arrastre":
                                mouse.press(button)
                                print(f"Presionado (arrastre): {button_str}")
                            else:
                                mouse.click(button)
                                print(f"Click normal (presionar y soltar): {button_str}")
                            print(f"Click con botón: {button_str}")
                        else:
                            print(f"Botón no reconocido: {button_str}")

                    elif tipo == "key":
                        key_str = msg.get("key", "")
                        if len(key_str) == 1:
                            keyboard.press(key_str)
                            keyboard.release(key_str)
                            print(f"Tecla: {key_str}")
                        else:
                            print(f"Tecla normal no válida: {key_str}")

                    elif tipo == "skey":
                        key_str = msg.get("key", "")
                        key = KEYS.get(key_str.lower())

                        if key:
                            if key in ONE_SHOT_KEYS:
                                keyboard.press(key)
                                keyboard.release(key)
                                print(f"🔁 Tecla especial de un solo uso: {key_str}")
                            elif key in pressed_keys:
                                keyboard.release(key)
                                pressed_keys.remove(key)
                                print(f"🔓 Tecla especial liberada: {key_str}")
                            else:
                                keyboard.press(key)
                                pressed_keys.add(key)
                                print(f"🔒 Tecla especial presionada: {key_str}")
                        else:
                            print(f"❌ Tecla especial no reconocida: {key_str}")

                except json.JSONDecodeError:
                    print("JSON inválido recibido:", line)

    except Exception as e:
        print(f"Error en conexión con {addr}: {e}")
    finally:
        conn.close()

def get_local_ip():
    try:
        # Crea un socket falso para obtener la IP local
        with socket.socket(socket.AF_INET, socket.SOCK_DGRAM) as s:
            s.connect(("8.8.8.8", 80))  # no envía datos
            return s.getsockname()[0]
    except Exception:
        return "127.0.0.1"


def udp_discovery_service():
    sock = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
    sock.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
    sock.bind(('', DISCOVERY_PORT))
    print(f"Servicio UDP de descubrimiento escuchando en puerto {DISCOVERY_PORT}...")

    while True:
        try:
            data, addr = sock.recvfrom(1024)
            message = data.decode().strip()
            print(f"Mensaje UDP recibido de {addr}: {message}")
            if message == "DISCOVER_REQUEST":
                # Responde con la IP y puerto TCP donde se encuentra el servidor
                server_ip = get_local_ip()
                response = f"DISCOVER_RESPONSE:{server_ip}:{PORT}"
                sock.sendto(response.encode(), addr)
                print(f"Respondido a {addr} con {response}")
        except Exception as e:
            print(f"Error en servicio UDP de descubrimiento: {e}")

def main():
    mover = MouseMover(factor=FACTOR)
    server = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    server.bind((HOST, PORT))
    server.listen(5)
    print(f"Servidor TCP escuchando en {HOST}:{PORT}...")

    # Iniciar hilo para servicio UDP discovery
    discovery_thread = threading.Thread(target=udp_discovery_service, daemon=True)
    discovery_thread.start()

    try:
        while True:
            conn, addr = server.accept()
            client_thread = threading.Thread(target=handle_client, args=(conn, addr, mover))
            client_thread.daemon = True
            client_thread.start()
    except KeyboardInterrupt:
        print("Cerrando servidor...")
    finally:
        mover.stop()
        server.close()

if __name__ == "__main__":
    main()
