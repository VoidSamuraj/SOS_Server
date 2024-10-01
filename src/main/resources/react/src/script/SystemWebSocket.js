/**
 * WebSocketClient class for managing a WebSocket connection.
 */
class SystemWebSocket {
  /**
   * Creates a new instance of WebSocketClient.
   *
   * @param {string} url - The URL for the WebSocket connection.
   * @param {function} onOpen - A function to be called when the connection is opened.
   */
  constructor(url, onOpen) {
    this.socket = new WebSocket(url);
    this.socket.onmessage = this.handleMessage.bind(this);
    this.socket.onopen = onOpen;
    this.socket.onclose = this.handleClose.bind(this);
    this.socket.onerror = this.handleError.bind(this);
    this.messageHandlers = [];
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
      return null;
    }
  }

  /**
   * Handles the WebSocket connection open event.
   */
  handleOpen() {
    console.log("WebSocket connection opened");
  }

  /**
   * Handles the WebSocket connection close event.
   */
  handleClose() {
    console.log("WebSocket connection closed");
  }

  /**
   * Handles the WebSocket error.
   */
  handleError(error) {
    console.error("WebSocket error:", error);
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
}

export default SystemWebSocket;
