import { NativeModules } from "react-native";

const { RNYuntiImageCropper } = NativeModules;

export default RNYuntiImageCropper;

class ImageCropperUtil {
  public cropWithUriWithAspectRatio = async (
    uri: string,
    param: { aspectRatioX: number; aspectRatioY: number }
  ): Promise<{ uri: string }> => {
    return RNYuntiImageCropper.cropWithUriWithAspectRatio(uri, param);
  };

  public cropWithUri = async (uri: string): Promise<{ uri: string }> => {
    return RNYuntiImageCropper.cropWithUri(uri);
  };
}

export const imageCropperUtil = new ImageCropperUtil();
