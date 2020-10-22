import { WebPlugin } from '@capacitor/core';
import { UnzipPlugin } from './definitions';

export class UnzipWeb extends WebPlugin implements UnzipPlugin {
  constructor() {
    super({
      name: 'Unzip',
      platforms: ['web'],
    });
  }

  async echo(options: { value: string }): Promise<{ value: string }> {
    console.log('ECHO', options);
    return options;
  }
}

const Unzip = new UnzipWeb();

export { Unzip };

import { registerWebPlugin } from '@capacitor/core';
registerWebPlugin(Unzip);
