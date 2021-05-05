import Foundation
import Capacitor
import SSZipArchive

/**
 * Please read the Capacitor iOS Plugin Development Guide
 * here: https://capacitorjs.com/docs/plugins/ios
 */
@objc(Unzip)
public class Unzip: CAPPlugin {

    @objc func unzipFile(_ call: CAPPluginCall) {
        let path = call.getString("path")?.replacingOccurrences(of: "file:///", with: "/")
        let password = call.getString("password")
        let destination = call.getString("destination")?.replacingOccurrences(of: "./", with: "").replacingOccurrences(of: "file:///", with: "/")
        
        if (path != nil && destination != nil) {
            do {
                let tempFolder = "temp"
                let destFolder = destination! + tempFolder + "/"
                try? FileManager.default.createDirectory(at: URL(string: "file://" + destFolder)!, withIntermediateDirectories: true, attributes: nil)
                try SSZipArchive.unzipFile(atPath: path!, toDestination: destFolder, overwrite: true, password: password)
                call.success([
                    "output": tempFolder
                ])
            } catch {
                print("Extract Failed: \(error.localizedDescription)")
                call.reject("Failed to extract zip", "EXTRACTION_FAILED")
            }
        } else {
            call.reject("Failed to find files", "FILE_NOT_FOUND")
        }
    }
}
