require "json"

package = JSON.parse(File.read(File.join(__dir__, "package.json")))

Pod::Spec.new do |s|
  s.name         = "RNYuntiImageCropper"
  s.version      = package["version"]
  s.summary      = package["description"]
  s.description  = <<-DESC
                  RNYuntiImageCropper
                   DESC
  s.homepage     = "https://git.bookln.cn/yuntitech_react_native/react-native-yunti-image-cropper"
  s.license      = "MIT"
  s.author       = { "yunti" => "" }
  s.platform     = :ios, "8.0"
  s.source       = { :git => "https://git.bookln.cn/yuntitech_react_native/react-native-yunti-image-cropper.git", :tag => "#{s.version}" }

  s.source_files = "ios/**/*.{h,m}"
  s.requires_arc = true

  s.dependency "React/Core"
end
