
#include <iostream>
using namespace cv;

//All write contour functions are experimental
void writeContourAll(cv::Mat& image) {
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

void writeContourMask(cv::Mat& image) {
    // Convert the image to grayscale
    cv::Mat gray;
    cv::cvtColor(image, gray, cv::COLOR_BGR2GRAY);

    // Threshold the grayscale image to create a binary mask
    cv::Mat mask;
    cv::threshold(gray, mask, 0, 255, cv::THRESH_BINARY_INV | cv::THRESH_OTSU);

    // Find contours in the mask
    std::vector<std::vector<cv::Point>> contours;
    cv::findContours(mask, contours, cv::RETR_EXTERNAL, cv::CHAIN_APPROX_SIMPLE);

    // Calculate centroids of contours
    std::vector<cv::Moments> mu(contours.size());
    for (size_t i = 0; i < contours.size(); i++) {
        mu[i] = cv::moments(contours[i]);
    }

    // Find the contour corresponding to the object in the center
    cv::Point2f imageCenter(static_cast<float>(image.cols / 2), static_cast<float>(image.rows / 2));
    int centerContourIndex = -1;
    float minDist = std::numeric_limits<float>::max();

    for (size_t i = 0; i < contours.size(); i++) {
        cv::Point2f centroid(static_cast<float>(mu[i].m10 / mu[i].m00), static_cast<float>(mu[i].m01 / mu[i].m00));
        float dist = cv::norm(imageCenter - centroid);

        if (dist < minDist) {
            minDist = dist;
            centerContourIndex = static_cast<int>(i);
        }
    }

    // Create a mask for the largest contour
    cv::Mat contourMask = cv::Mat::zeros(image.size(), CV_8UC1);
    cv::drawContours(contourMask, contours, centerContourIndex, cv::Scalar(255), cv::FILLED);

    // Apply the mask to the original image to keep only the part inside the contour
    cv::Mat result;
    image.copyTo(result, contourMask);

    // Update the original image with the result
    image = result;
}

void writeContour(cv::Mat& image) {
    // Convert the image to grayscale
    cv::Mat gray;
    cv::cvtColor(image, gray, cv::COLOR_BGR2GRAY);

    // Threshold the grayscale image to create a binary mask
    cv::Mat mask;
    cv::threshold(gray, mask, 0, 255, cv::THRESH_BINARY_INV | cv::THRESH_OTSU);

    // Find contours in the mask
    std::vector<std::vector<cv::Point>> contours;
    cv::findContours(mask, contours, cv::RETR_EXTERNAL, cv::CHAIN_APPROX_SIMPLE);

    // Calculate centroids of contours
    std::vector<cv::Moments> mu(contours.size());
    for (size_t i = 0; i < contours.size(); i++) {
        mu[i] = cv::moments(contours[i]);
    }

    // Find the contour corresponding to the object in the center
    cv::Point2f imageCenter(static_cast<float>(image.cols / 2), static_cast<float>(image.rows / 2));
    int centerContourIndex = -1;
    float minDist = std::numeric_limits<float>::max();

    for (size_t i = 0; i < contours.size(); i++) {
        cv::Point2f centroid(static_cast<float>(mu[i].m10 / mu[i].m00), static_cast<float>(mu[i].m01 / mu[i].m00));
        float dist = cv::norm(imageCenter - centroid);

        if (dist < minDist) {
            minDist = dist;
            centerContourIndex = static_cast<int>(i);
        }
    }

    // Draw the contour of the center object onto the image
    cv::drawContours(image, contours, centerContourIndex, cv::Scalar(0, 255, 0), 2);
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

int getAverageHSV(const cv::Mat& image) {
    // Get image dimensions
    int rows = image.rows;
    int cols = image.cols;

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
    int range = 3;
    cv::Mat resultImage;
    cv::Mat mask;

    int hsvValue;
    hsvValue = getAverageHSV(image);

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

            if (h < 5) {
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
    int range = 3; 
    cv::Mat resultImage;
    cv::Mat mask;

    //Exists to test the write contour functions
    if (true) {
        // Write contour of the central object
        writeContourAll(image);

        // Display the result
        cv::imshow("Result Image", image);
        cv::imwrite("C:/Users/Sebastian WL/Desktop/Results/img.png", image);
        cv::waitKey(0);

        return 0;
    }

    int hsvValue;
    if (false) {
        hsvValue = getHSV(image);
    }
    else {
        hsvValue = getAverageHSV(image);
    }

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

            if (h < 5) {
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

            cv::imshow("Input Image", image);
            cv::imshow("Result (Masked) Image", resultImage);
        }
        else {
            int minHue = h - range, maxHue = h + range;

            lowerBound = cv::Scalar(minHue, minSat, minVal);
            upperBound = cv::Scalar(maxHue, maxSat, maxVal);

            mask = createMask(image, lowerBound, upperBound);

            resultImage = applyMask(image, mask);


            cv::imshow("Input Image", image);
            cv::imshow("Result (Masked) Image", resultImage);
        }

        cv::imwrite("C:/Users/Sebastian WL/Desktop/Results/img.png", resultImage);
    }
    waitKey(0);

    return 0;
}

int main() {
    std::string imgPath = "C:/Users/Sebastian WL/Desktop/Images/stop.jpg";

    cv::Mat image;

    image = readImage(imgPath);

    identifyObject(image);

    return 0;
}
