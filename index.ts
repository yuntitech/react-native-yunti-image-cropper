import { NativeModules } from "react-native";

const { RNYuntiImageCropper } = NativeModules;

export default RNYuntiImageCropper;

/**
 * left,right,top,bottom表示裁剪之后的图片占原图坐标值，大小为0～1
 */
export interface CroppedCoordsPercent {
  left: number;
  right: number;
  top: number;
  bottom: number;
}

export interface CroppedData {
  uri: string;

  /**
   * 旋转之后未裁剪的图片
   *
   * @platform iOS
   */
  originalRotatedUri?: string;

  croppedCoordsPercent?: CroppedCoordsPercent;
  croppedWidth?: number;
  croppedHeight?: number;
}

class ImageCropperUtil {
  public cropWithUriWithAspectRatio = async (
    uri: string,
    param: { aspectRatioX: number; aspectRatioY: number }
  ): Promise<CroppedData> => {
    return RNYuntiImageCropper.cropWithUriWithAspectRatio(uri, param);
  };

  public cropWithUri = async (uri: string): Promise<CroppedData> => {
    return RNYuntiImageCropper.cropWithUri(uri);
  };
}

export const imageCropperUtil = new ImageCropperUtil();
