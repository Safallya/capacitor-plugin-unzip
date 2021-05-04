import Foundation
import Capacitor
import Zip

/**
 * Please read the Capacitor iOS Plugin Development Guide
 * here: https://capacitorjs.com/docs/plugins/ios
 */
@objc(Unzip)
public class Unzip: CAPPlugin {

    @objc func unzipFile(_ call: CAPPluginCall) {
        let path = URL(string: call.getString("path") ?? "")
        let password = call.getString("password")
        let destination = URL(string: call.getString("destination") ?? "")
        
        if (path != nil && destination != nil) {
            do {
                let tempFolder = "temp"
                try Zip.unzipFile(path!, destination: destination!.appendingPathComponent(tempFolder), overwrite: true, password: password)
                call.success([
                    "output": tempFolder
                ])
            } catch {
                call.reject("Failed to extract zip", "EXTRACTION_FAILED")
            }
        } else {
            call.reject("Failed to find files", "FILE_NOT_FOUND")
        }
    }
}
