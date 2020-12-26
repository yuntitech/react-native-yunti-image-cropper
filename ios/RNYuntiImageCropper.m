#import "RNYuntiImageCropper.h"
#import "TOCropViewController.h"

@interface RNYuntiImageCropper () <TOCropViewControllerDelegate>

@property (nonatomic, strong) RCTPromiseRejectBlock _reject;
@property (nonatomic, strong) RCTPromiseResolveBlock _resolve;
@property (nonatomic, nullable) UIImage *originalImage;

@end

@implementation RNYuntiImageCropper

RCT_EXPORT_MODULE()

@synthesize bridge = _bridge;

- (dispatch_queue_t)methodQueue {
  return dispatch_get_main_queue();
}

RCT_EXPORT_METHOD(cropWithUri:(NSString *)imageUrl
                  resolver:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject) {
  if (!imageUrl) {
    reject(@"500", @"文件路径为空", [NSError errorWithDomain:@"文件路径为空" code:500 userInfo:NULL]);
    return;
  }
  self._reject = reject;
  self._resolve = resolve;
  NSURLRequest *imageUrlrequest = [NSURLRequest requestWithURL:[NSURL URLWithString:imageUrl]];
  
  __weak __typeof(self) weakSelf = self;
  [self.bridge.imageLoader loadImageWithURLRequest:imageUrlrequest callback:^(NSError *error, UIImage *image) {
    if (error) {
      reject(@"500", @"加载图片失败", error);
      return;
    }
    if (image) {
      [weakSelf handleImageLoad:image aspectRatio:CGSizeZero];
      weakSelf.originalImage = image;
    }
  }];
}

RCT_EXPORT_METHOD(cropWithUriWithAspectRatio:(NSString *)imageUrl
                  params:(NSDictionary *)params
                  resolver:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject) {
  if (!imageUrl) {
    reject(@"500", @"文件路径为空", [NSError errorWithDomain:@"文件路径为空" code:500 userInfo:NULL]);
    return;
  }
  
  self._reject = reject;
  self._resolve = resolve;
  NSURLRequest *imageUrlrequest = [NSURLRequest requestWithURL:[NSURL URLWithString:imageUrl]];
  
  __weak __typeof(self) weakSelf = self;
  [self.bridge.imageLoader loadImageWithURLRequest:imageUrlrequest callback:^(NSError *error, UIImage *image) {
    if (error) {
      reject(@"500", @"加载图片失败", error);
      return;
    }
    if (image) {
      NSNumber *width = [params valueForKey:@"aspectRatioX"];
      NSNumber *height = [params valueForKey:@"aspectRatioY"];
      CGSize ratio = CGSizeMake(width.doubleValue, height.doubleValue);
      [weakSelf handleImageLoad:image aspectRatio:ratio];
    }
  }];
}

- (void)handleImageLoad:(UIImage *)image aspectRatio:(CGSize)aspectRatio {
  TOCropViewController *cropViewController = [[TOCropViewController alloc] initWithImage:image];
  
  if (!CGSizeEqualToSize(CGSizeZero, aspectRatio)) {
    cropViewController.customAspectRatio = aspectRatio;
  }
  
  cropViewController.delegate = self;
  dispatch_async(dispatch_get_main_queue(), ^{
    UIViewController *root = [[[[UIApplication sharedApplication] delegate] window] rootViewController];
    [root presentViewController:cropViewController animated:YES completion:nil];
  });
}

- (void)cropViewController:(TOCropViewController *)cropViewController didCropToImage:(UIImage *)image withRect:(CGRect)cropRect angle:(NSInteger)angle {
  dispatch_async(dispatch_get_main_queue(), ^{
    [cropViewController dismissViewControllerAnimated:YES completion:nil];
  });
  
  NSData *pngData = UIImagePNGRepresentation(image);
  NSTimeInterval now = [NSDate timeIntervalSinceReferenceDate];
  NSString *fileName = [NSString stringWithFormat:@"yunti-rn-crop-%lf.png", now];
  NSString *filePath = [NSTemporaryDirectory() stringByAppendingPathComponent:fileName];
  [pngData writeToFile:filePath atomically:YES];
  NSNumber *width  = [NSNumber numberWithFloat:image.size.width];
  NSNumber *height = [NSNumber numberWithFloat:image.size.height];
  
  UIImage *originalRotatedImage = [self rotateImage:self.originalImage degrees:angle];
  
  // 保存旋转之后的原图
  NSData *originalRotatedData = UIImagePNGRepresentation(originalRotatedImage);
  NSString *originalRotatedFileName = [NSString stringWithFormat:@"yunti-rn-crop-original-rotated-%lf.png", now];
  NSString *originalRotatedFilePath = [NSTemporaryDirectory() stringByAppendingPathComponent:originalRotatedFileName];
  [originalRotatedData writeToFile:originalRotatedFilePath atomically:YES];
  
  CGFloat left = cropRect.origin.x / originalRotatedImage.size.width;
  CGFloat right = (cropRect.origin.x + cropRect.size.width) / originalRotatedImage.size.width;
  CGFloat top = cropRect.origin.y / originalRotatedImage.size.height;
  CGFloat bottom = (cropRect.origin.y + cropRect.size.height) / originalRotatedImage.size.height;
  NSDictionary<NSString *, NSNumber *> *croppedCoordsPercent = @{
    @"left": @(left),
    @"right": @(right),
    @"top": @(top),
    @"bottom": @(bottom),
  };
  NSDictionary * imageData = @{
    @"uri": filePath,
    @"originalRotatedUri": originalRotatedFilePath,
    @"croppedWidth": width,
    @"croppedHeight": height,
    @"croppedCoordsPercent": croppedCoordsPercent
  };
  
  self._resolve(imageData);
  self.originalImage = nil;
}

- (void)cropViewController:(TOCropViewController *)cropViewController didFinishCancelled:(BOOL)cancelled {
  dispatch_async(dispatch_get_main_queue(), ^{
    [cropViewController dismissViewControllerAnimated:YES completion:nil];
  });
  self._reject(@"501", @"取消操作", [NSError errorWithDomain:@"取消操作" code:501 userInfo:NULL]);
}

/**
 copied from https://github.com/shaojiankui/JKCategories/blob/master/JKCategories/UIKit/UIImage/UIImage%2BJKOrientation.m
 */
- (UIImage *)rotateImage:(UIImage *)image degrees:(CGFloat)degrees {
  // calculate the size of the rotated view's containing box for our drawing space
  UIView *rotatedViewBox = [[UIView alloc] initWithFrame:CGRectMake(0, 0, image.size.width, image.size.height)];
  CGAffineTransform t = CGAffineTransformMakeRotation([self degreesToRadians:degrees]);
  rotatedViewBox.transform = t;
  CGSize rotatedSize = rotatedViewBox.frame.size;
  
  // Create the bitmap context
  UIGraphicsBeginImageContext(rotatedSize);
  CGContextRef bitmap = UIGraphicsGetCurrentContext();
  
  // Move the origin to the middle of the image so we will rotate and scale around the center.
  CGContextTranslateCTM(bitmap, rotatedSize.width/2, rotatedSize.height/2);
  
  //   // Rotate the image context
  CGContextRotateCTM(bitmap, [self degreesToRadians:degrees]);
  
  // Now, draw the rotated/scaled image into the context
  CGContextScaleCTM(bitmap, 1.0, -1.0);
  CGContextDrawImage(bitmap, CGRectMake(-image.size.width / 2, -image.size.height / 2, image.size.width, image.size.height), [image CGImage]);
  
  UIImage *newImage = UIGraphicsGetImageFromCurrentImageContext();
  UIGraphicsEndImageContext();
  return newImage;
}

- (CGFloat)degreesToRadians:(CGFloat)degrees {
  return degrees * M_PI / 180;
}

@end
