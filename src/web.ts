import { WebPlugin } from '@capacitor/core';
import { UnzipPlugin } from './definitions';

export class UnzipWeb extends WebPlugin implements UnzipPlugin {
  constructor() {
    super({
      name: 'Unzip',
      platforms: ['web'],
    });
  }

  async unzipFile(options: any): Promise<any> {
    return null;
  }
}

const Unzip = new UnzipWeb();

export { Unzip };

import { registerWebPlugin } from '@capacitor/core';
registerWebPlugin(Unzip);
