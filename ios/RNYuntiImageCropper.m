#import "RNYuntiImageCropper.h"
#import "TOCropViewController.h"

@interface RNYuntiImageCropper () <TOCropViewControllerDelegate>

@property (nonatomic, strong) RCTPromiseRejectBlock _reject;
@property (nonatomic, strong) RCTPromiseResolveBlock _resolve;

@end

@implementation RNYuntiImageCropper

RCT_EXPORT_MODULE()

@synthesize bridge = _bridge;

- (dispatch_queue_t)methodQueue
{
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
    [self.bridge.imageLoader loadImageWithURLRequest:imageUrlrequest callback:^(NSError *error, UIImage *image) {
        if(error) reject(@"500", @"加载图片失败", error);
        if(image) {
            [self handleImageLoad:image];
        }
    }];
}

- (void)handleImageLoad:(UIImage *)image {
    TOCropViewController *cropViewController = [[TOCropViewController alloc] initWithImage:image];
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
    NSString *fileName = [NSString stringWithFormat:@"yunti-rn-crop-%lf.png", [NSDate timeIntervalSinceReferenceDate]];
    NSString *filePath = [NSTemporaryDirectory() stringByAppendingPathComponent:fileName];
    [pngData writeToFile:filePath atomically:YES];
    NSNumber *width  = [NSNumber numberWithFloat:image.size.width];
    NSNumber *height = [NSNumber numberWithFloat:image.size.height];
    
    NSDictionary * imageData = @{
                                 @"uri":filePath,
                                 @"width":width,
                                 @"height":height
                                 };
    self._resolve(imageData);
}

- (void)cropViewController:(TOCropViewController *)cropViewController didFinishCancelled:(BOOL)cancelled {
    dispatch_async(dispatch_get_main_queue(), ^{
        [cropViewController dismissViewControllerAnimated:YES completion:nil];
    });
    self._reject(@"501", @"取消操作", [NSError errorWithDomain:@"取消操作" code:501 userInfo:NULL]);
}

@end
