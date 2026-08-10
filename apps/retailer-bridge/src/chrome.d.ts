declare const chrome: {
  runtime: {
    sendMessage(message: unknown): Promise<unknown>;
  };
};
