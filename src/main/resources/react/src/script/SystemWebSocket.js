/**
 * WebSocketClient class for managing a WebSocket connection.
 */
class SystemWebSocket {
  /**
   * Creates a new instance of WebSocketClient.
   *
   * @param {string} url - The URL for the WebSocket connection.
   * @param {function} onOpen - A function to be called when the connection is opened.
   * @param {function} onClose - A function to be called when the connection is closed.
   */
  constructor(url, onOpen, onClose) {
    this.url = url; // Przechowuje URL dla połączenia
    this.onOpen = onOpen; // Przechowuje referencję do funkcji onOpen
    this.retryCount = 0;
    this.executeOnLostConnection = onClose; // Używane w przypadku utraty połączenia
    this.messageHandlers = [];
    this.connect(); // Inicjalizuje połączenie
  }

  /**
   * Connects the WebSocket.
   */
  connect() {
    this.socket = new WebSocket(this.url);
    this.socket.onmessage = this.handleMessage.bind(this);
    this.socket.onopen = () => {
      this.retryCount = 0; // Resetuje licznik prób przy pomyślnym połączeniu
      if (this.onOpen) {
        this.onOpen(); // Wywołuje funkcję onOpen
      }
      console.log("WebSocket connection opened");
    };
    this.socket.onclose = this.handleClose.bind(this);
    this.socket.onerror = this.handleError.bind(this);
  }

  /**
   * Handles incoming WebSocket messages.
   *
   * @param {MessageEvent} event - The event object containing the message data.
   */
  handleMessage(event) {
    try {
      const message = JSON.parse(event.data);
      this.messageHandlers.forEach((handler) => handler(message));
    } catch (error) {
      console.error("Error parsing JSON:", error);
    }
  }

  /**
   * Handles the WebSocket connection close event.
   */
  handleClose() {
    console.log("WebSocket connection closed");
    this.executeOnLostConnection();
    if (this.socket.readyState === WebSocket.CLOSED) {
      this.retryCount++;
      this.reconnect();

    }
  }

  /**
   * Handles the WebSocket error.
   */
  handleError(error) {
    console.error("WebSocket error:", error);
    this.executeOnLostConnection();
    if (this.socket.readyState === WebSocket.CLOSED) {
      this.retryCount++;
      this.reconnect();
    }
  }

  /**
   * Sends data to the WebSocket server.
   *
   * @param {Object} data - The data object to be sent.
   */
  send(data) {
    if (this.socket.readyState === WebSocket.OPEN) {
      this.socket.send(JSON.stringify(data));
    }
  }

  /**
   * Adds a handler for processing incoming messages.
   *
   * @param {function} handler - The function to be called for each message.
   */
  addMessageHandler(handler) {
    this.messageHandlers.push(handler);
  }

  /**
   * Removes a handler from the message processing list.
   *
   * @param {function} handler - The function to be removed.
   */
  removeMessageHandler(handler) {
    this.messageHandlers = this.messageHandlers.filter((h) => h !== handler);
  }

  /**
   * Closes the WebSocket connection.
   */
  close() {
    this.socket.close();
  }

  /**
   * Reconnects the WebSocket after a delay.
   */
  reconnect() {
    const retryDelay = 10000;
    setTimeout(() => {
      console.log(`Reconnecting attempt ${this.retryCount} after ${retryDelay / 1000} seconds`);
      this.connect(); // Ponownie łączy się
    }, retryDelay);
  }
}

export default SystemWebSocket;