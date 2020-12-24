import { NativeModules } from "react-native";

const { RNYuntiImageCropper } = NativeModules;

export default RNYuntiImageCropper;

/**
 * left,right,top,bottom表示裁剪之后的图片占原图坐标值，大小为0～1
 */
export interface CropCoordsPercent {
  left: number;
  right: number;
  top: number;
  bottom: number;
}

class ImageCropperUtil {
  public cropWithUriWithAspectRatio = async (
    uri: string,
    param: { aspectRatioX: number; aspectRatioY: number }
  ): Promise<{
    uri: string;
    croppedCoordsPercent?: CropCoordsPercent;
    croppedWidth?: number;
    croppedHeight?: number;
  }> => {
    return RNYuntiImageCropper.cropWithUriWithAspectRatio(uri, param);
  };

  public cropWithUri = async (
    uri: string
  ): Promise<{
    uri: string;
    croppedCoordsPercent?: CropCoordsPercent;
    croppedWidth?: number;
    croppedHeight?: number;
  }> => {
    return RNYuntiImageCropper.cropWithUri(uri);
  };
}

export const imageCropperUtil = new ImageCropperUtil();
