#include <opencv2/core.hpp>
#include <iostream>
#include <opencv2/imgproc.hpp>
#include <opencv2/imgcodecs.hpp>
#include <opencv2/opencv.hpp>
#include <jni.h>
#include <string>

using namespace cv;


std::vector<std::vector<cv::Point>> getContours(cv::Mat& image) {
    //cv::Mat filteredImage;
    //cv::bilateralFilter(image, filteredImage, 9, 75, 75);  // Adjust parameters as needed
    //image = filteredImage;

    cv::Mat gray;
    cv::cvtColor(image, gray, cv::COLOR_BGR2GRAY);

    // Threshold the grayscale image to create a binary mask
    cv::Mat mask;
    cv::threshold(gray, mask, 0, 255, cv::THRESH_BINARY_INV | cv::THRESH_OTSU);

    // Find contours in the mask
    std::vector<std::vector<cv::Point>> contours;
    cv::findContours(mask, contours, cv::RETR_EXTERNAL, cv::CHAIN_APPROX_SIMPLE);

    std::vector<std::vector<cv::Point>> filteredContours;
    int minArea = 1000;

    for (const auto& contour : contours) {
        double area = contourArea(contour);
        if (area >= minArea) {
            filteredContours.push_back(contour);
        }
    }

    return filteredContours;
}

cv::Mat findObject(cv::Mat image, int x, int y) {

    // Create a point for the specific pixel
    cv::Point point(x, y);

    std::vector<std::vector<cv::Point>> contours = getContours(image);

    // Check if the specific pixel is within any contour
    for (const auto& contour : contours) {
        if (cv::pointPolygonTest(contour, point, false) >= 0) {
            // Draw the contour containing the specific pixel
            cv::drawContours(image, std::vector<std::vector<cv::Point>>{contour}, -1, cv::Scalar(0, 255, 0), 20);
            cv::circle(image, point, 5, cv::Scalar(255, 0, 0), -1); // Draw the specific pixel
            break;
        }
    }

    return image;
}

int findObjectArea(cv::Mat image, int x, int y) {
    // Create a point for the specific pixel
    cv::Point point(x, y);

    std::vector<std::vector<cv::Point>> contours = getContours(image);
    double area = 0;

    // Check if the specific pixel is within any contour
    for (const auto& contour : contours) {
        if (cv::pointPolygonTest(contour, point, false) >= 0) {
            //Find the area of the object
            area = cv::contourArea(contour);

            break;
        }
    }

    return area;
}

cv::Mat writeAllContour(cv::Mat& image) {

    std::vector<std::vector<cv::Point>> contours = getContours(image);

    // Draw contours on the original image
    cv::drawContours(image, contours, -1, cv::Scalar(0, 255, 0), 20);

    return image;
}

void removeBackground(cv::Mat& image) {
    // Convert the image to grayscale
    cv::Mat gray;
    cv::cvtColor(image, gray, cv::COLOR_BGR2GRAY);

    // Threshold the grayscale image to create a binary mask
    cv::Mat mask;
    cv::threshold(gray, mask, 0, 255, cv::THRESH_BINARY_INV | cv::THRESH_OTSU);

    // Find contours in the mask
    std::vector<std::vector<cv::Point>> contours;
    cv::findContours(mask, contours, cv::RETR_EXTERNAL, cv::CHAIN_APPROX_SIMPLE);

    // Create a mask for the contours
    cv::Mat contourMask = cv::Mat::zeros(image.size(), CV_8UC1);
    cv::drawContours(contourMask, contours, -1, cv::Scalar(255), cv::FILLED);

    // Apply the mask to the original image to keep only the objects inside the contours
    cv::Mat result;
    image.copyTo(result, contourMask);

    // Update the original image with the result
    image = result;
}

int identifyCenterObjectArea(cv::Mat image) {

    std::vector<std::vector<cv::Point>> contours = getContours(image);

    // Calculate centroids of contours
    std::vector<cv::Moments> mu(contours.size());
    for (size_t i = 0; i < contours.size(); i++) {
        mu[i] = cv::moments(contours[i]);
    }

    // Find the contour corresponding to the object in the center
    cv::Point2f imageCenter(static_cast<float>(image.cols / 2), static_cast<float>(image.rows / 2));
    int centerContourIndex = -1;
    float minDist = std::numeric_limits<float>::max();

    double area = 0;

    for (size_t i = 0; i < contours.size(); i++) {
        cv::Point2f centroid(static_cast<float>(mu[i].m10 / mu[i].m00), static_cast<float>(mu[i].m01 / mu[i].m00));
        float dist = cv::norm(imageCenter - centroid);

        if (dist < minDist) {
            minDist = dist;
            area = cv::contourArea(contours[i]);
        }
    }

    return area;
}

cv::Mat identifyCenterObject(cv::Mat image) {

    std::vector<std::vector<cv::Point>> contours = getContours(image);

    // Calculate centroids of contours
    std::vector<cv::Moments> mu(contours.size());
    for (size_t i = 0; i < contours.size(); i++) {
        mu[i] = cv::moments(contours[i]);
    }

    // Find the contour corresponding to the object in the center
    cv::Point2f imageCenter(static_cast<float>(image.cols / 2), static_cast<float>(image.rows / 2));
    int centerContourIndex = -1;
    float minDist = std::numeric_limits<float>::max();

    double area = 0;

    for (size_t i = 0; i < contours.size(); i++) {
        cv::Point2f centroid(static_cast<float>(mu[i].m10 / mu[i].m00), static_cast<float>(mu[i].m01 / mu[i].m00));
        float dist = cv::norm(imageCenter - centroid);

        if (dist < minDist) {
            minDist = dist;
            centerContourIndex = static_cast<int>(i);
        }
    }

    // Draw the contour of the center object onto the image
    cv::drawContours(image, contours, centerContourIndex, cv::Scalar(0, 255, 0), 4);

    return image;
}

cv::Mat readImage(const std::string& imgPath) {
    // Read the image
    cv::Mat image = cv::imread(imgPath, cv::IMREAD_COLOR);

    if (image.empty()) {
        std::cerr << "Error: Could not open the image file." << std::endl;
        return cv::Mat();  // Return an empty Mat if the image could not be opened
    }

    return image;
}

int getHSV(const cv::Mat srcImage) {

    if (srcImage.empty()) {
        std::cerr << "Error: Could not open the image file." << std::endl;
        return -1;
    }

    // Get image dimensions
    int rows = srcImage.rows;
    int cols = srcImage.cols;

    // Calculate the coordinates of the pixel in the middle of the image
    int centerX = cols / 2;
    int centerY = rows / 2;

    // Convert BGR to HSV
    cv::Mat3b bgrImage(srcImage);
    cv::Mat3b hsvImage;
    cv::cvtColor(bgrImage, hsvImage, cv::COLOR_BGR2HSV);

    // Get the HSV values of the pixel in the middle of the image
    cv::Vec3b hsvColor = hsvImage(centerY, centerX);

    int hsvValue = (hsvColor[0] << 16) | (hsvColor[1] << 8) | hsvColor[2];

    return hsvValue;
}

int getAverageHSV(const cv::Mat& image) {
    // Get image dimensions
    int rows = image.rows;
    int cols = image.cols;

    if (cols < 8 || rows < 8){
        return getHSV(image);
    }

    //Should maybe make it dependant on the size of the image
    // Calculate the region of interest (ROI) in the middle of the image
    int startX = cols / 2 - 4;
    int endX = cols / 2 + 4;
    int startY = rows / 2 - 4;
    int endY = rows / 2 + 4;

    // Initialize accumulators for HSV values
    double totalH = 0, totalS = 0, totalV = 0;
    int numPixels = 0;

    // Iterate over the ROI to accumulate HSV values
    for (int y = startY; y < endY; y++) {
        for (int x = startX; x < endX; x++) {
            cv::Vec3b bgrPixel = image.at<cv::Vec3b>(y, x);
            cv::Mat bgrMat(1, 1, CV_8UC3);
            bgrMat.at<cv::Vec3b>(0, 0) = bgrPixel;

            cv::Mat hsvMat;
            cv::cvtColor(bgrMat, hsvMat, cv::COLOR_BGR2HSV);

            cv::Vec3b hsvPixel = hsvMat.at<cv::Vec3b>(0, 0);

            totalH += hsvPixel[0];
            totalS += hsvPixel[1];
            totalV += hsvPixel[2];
            numPixels++;
        }
    }

    // Calculate average HSV values
    int avgH = static_cast<int>(totalH / numPixels);
    int avgS = static_cast<int>(totalS / numPixels);
    int avgV = static_cast<int>(totalV / numPixels);

    // Combine average HSV values into a single integer
    int hsvValue = (avgH << 16) | (avgS << 8) | avgV;

    return hsvValue;
}

cv::Mat createMask(const cv::Mat& inputImage, const cv::Scalar& lowerBound, const cv::Scalar& upperBound) {
    // Convert image to HSV color space
    cv::Mat inputImageHSV;
    cv::cvtColor(inputImage, inputImageHSV, cv::COLOR_BGR2HSV);

    // Create mask
    cv::Mat mask;
    cv::inRange(inputImageHSV, lowerBound, upperBound, mask);

    return mask;
}

cv::Mat applyMask(const cv::Mat& image, const cv::Mat& mask) {
    cv::Mat resultImage;
    cv::bitwise_and(image, image, resultImage, mask);

    return resultImage;
}

cv::Mat identifyObject(cv::Mat image) {
    int range = 4;
    cv::Mat resultImage;
    cv::Mat mask;

    int hsvValue = getAverageHSV(image);

    if (hsvValue != -1) {
        int h = (hsvValue >> 16) & 0xFF;
        int s = (hsvValue >> 8) & 0xFF;
        int v = hsvValue & 0xFF;

        int minSat = 100, maxSat = 255;
        int minVal = 0, maxVal = 255;

        cv::Scalar lowerBound;
        cv::Scalar upperBound;

        if (h < range || h >(180 - range)) {

            cv::Scalar lowerBound2;
            cv::Scalar upperBound2;

            cv::Mat mask1, mask2;

            if (h < range) {
                int minHue1 = 0, maxHue1 = h + range;
                int minHue2 = 179 - (range - h), maxHue2 = 180;

                lowerBound = cv::Scalar(minHue1, minSat, minVal);
                upperBound = cv::Scalar(maxHue1, maxSat, maxVal);

                lowerBound2 = cv::Scalar(minHue2, minSat, minVal);
                upperBound2 = cv::Scalar(maxHue2, maxSat, maxVal);
            }
            else {
                int minHue1 = h - range, maxHue1 = 180;
                int minHue2 = 0, maxHue2 = range - (180 - h);

                lowerBound = cv::Scalar(minHue1, minSat, minVal);
                upperBound = cv::Scalar(maxHue1, maxSat, maxVal);

                lowerBound2 = cv::Scalar(minHue2, minSat, minVal);
                upperBound2 = cv::Scalar(maxHue2, maxSat, maxVal);
            }

            mask1 = createMask(image, lowerBound, upperBound);
            mask2 = createMask(image, lowerBound2, upperBound2);

            mask = mask1 | mask2;

            resultImage = applyMask(image, mask);
        }
        else {
            int minHue = h - range, maxHue = h + range;

            lowerBound = cv::Scalar(minHue, minSat, minVal);
            upperBound = cv::Scalar(maxHue, maxSat, maxVal);

            mask = createMask(image, lowerBound, upperBound);

            resultImage = applyMask(image, mask);
        }
    }

    return resultImage;
}

int identifyObjectTest(cv::Mat image) {
    int range = 4;
    cv::Mat resultImage;
    cv::Mat mask;

    //Exists to test the write contour functions
    if (true) {
        //writeAllContour(image);
        image = findObject(image, 270, 180);
    }

    int hsvValue = getAverageHSV(image);

    if (hsvValue != -1) {
        int h = (hsvValue >> 16) & 0xFF;
        int s = (hsvValue >> 8) & 0xFF;
        int v = hsvValue & 0xFF;

        std::cout << "HSV Color in the middle of the image: "
                  << "H: " << h << ", "
                  << "S: " << s << ", "
                  << "V: " << v << std::endl;

        int minSat = 100, maxSat = 255;
        int minVal = 0, maxVal = 255;

        cv::Scalar lowerBound;
        cv::Scalar upperBound;

        if (h < range || h >(180 - range)) {

            cv::Scalar lowerBound2;
            cv::Scalar upperBound2;

            cv::Mat mask1, mask2;

            if (h < range) {
                int minHue1 = 0, maxHue1 = h + range;
                int minHue2 = 179 - (range - h), maxHue2 = 180;

                lowerBound = cv::Scalar(minHue1, minSat, minVal);
                upperBound = cv::Scalar(maxHue1, maxSat, maxVal);

                lowerBound2 = cv::Scalar(minHue2, minSat, minVal);
                upperBound2 = cv::Scalar(maxHue2, maxSat, maxVal);
            }
            else {
                int minHue1 = h - range, maxHue1 = 180;
                int minHue2 = 0, maxHue2 = range - (180 - h);

                lowerBound = cv::Scalar(minHue1, minSat, minVal);
                upperBound = cv::Scalar(maxHue1, maxSat, maxVal);

                lowerBound2 = cv::Scalar(minHue2, minSat, minVal);
                upperBound2 = cv::Scalar(maxHue2, maxSat, maxVal);
            }

            mask1 = createMask(image, lowerBound, upperBound);
            mask2 = createMask(image, lowerBound2, upperBound2);

            mask = mask1 | mask2;

            resultImage = applyMask(image, mask);
        }
        else {
            int minHue = h - range, maxHue = h + range;

            lowerBound = cv::Scalar(minHue, minSat, minVal);
            upperBound = cv::Scalar(maxHue, maxSat, maxVal);

            mask = createMask(image, lowerBound, upperBound);

            resultImage = applyMask(image, mask);
        }

        cv::imshow("Input Image", image);
        cv::imshow("Result (Masked) Image", resultImage);

        cv::imwrite("C:/Users/Sebastian WL/Desktop/Results/img.png", resultImage);
    }
    waitKey(0);

    return 0;
}

extern "C"
JNIEXPORT void JNICALL
Java_com_example_blodpool_MainActivity_cvTest(JNIEnv *env, jobject thiz, jlong mat_addy, jlong mat_addy_res, jint x_addy, jint y_addy) {

    //cv::cvtColor(matIn, matOut, cv::COLOR_BGR2GRAY);
    cv::Mat &mat = *(cv::Mat*) mat_addy;

    cv::Mat &resMat = *(cv::Mat*) mat_addy_res;

    int x = static_cast<int>(x_addy);
    int y = static_cast<int>(y_addy);

    resMat = findObject(mat, x, y);
}


int main() {


    return 0;
}
