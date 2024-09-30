// WebSocket.js

class WebSocketClient {
  constructor(url) {
    this.socket = new WebSocket(url);
    this.socket.onmessage = this.handleMessage.bind(this);
    this.socket.onopen = this.handleOpen.bind(this);
    this.socket.onclose = this.handleClose.bind(this);
    this.socket.onerror = this.handleError.bind(this);
    this.messageHandlers = [];
  }

  handleMessage(event) {
    try {
      const message = JSON.parse(event.data);
      this.messageHandlers.forEach((handler) => handler(message));
    } catch (error) {
      console.error("Error parsing JSON:", error);
      return null;
    }
  }

  handleOpen() {
    console.log("WebSocket connection opened");
  }

  handleClose() {
    console.log("WebSocket connection closed");
  }

  handleError(error) {
    console.error("WebSocket error:", error);
  }

  send(data) {
    if (this.socket.readyState === WebSocket.OPEN) {
      this.socket.send(JSON.stringify(data));
    }
  }

  addMessageHandler(handler) {
    this.messageHandlers.push(handler);
  }

  removeMessageHandler(handler) {
    this.messageHandlers = this.messageHandlers.filter((h) => h !== handler);
  }

  close() {
    this.socket.close();
  }
}

export default WebSocketClient;
