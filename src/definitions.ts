declare module '@capacitor/core' {
  interface PluginRegistry {
    Unzip: UnzipPlugin;
  }
}

export interface UnzipPlugin {
  echo(options: { value: string }): Promise<{ value: string }>;
}
