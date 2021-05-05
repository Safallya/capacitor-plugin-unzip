declare module '@capacitor/core' {
  interface PluginRegistry {
    Unzip: UnzipPlugin;
  }
}

export interface UnzipPlugin {
  unzipFile(options: any): Promise<any>;
}
