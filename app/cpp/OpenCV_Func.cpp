#include <opencv2/opencv.hpp>
#include <iostream>
#include <jni.h>

using namespace cv;



// Global vector to store contours
std::vector<std::vector<cv::Point>> contoursList;
cv::Scalar contourColor = cv::Scalar(222, 181, 255);


int areaInfo;
cv::Mat imageInfo;
cv::Point centerInfo;

double getArea() {
    return areaInfo;
}

cv::Mat getImage() {
    return imageInfo;
}

cv::Point getCenter() {
    return centerInfo;
}

void drawWeightedContour(cv::Mat image, std::vector<cv::Point> contour) {
    cv::Mat mask = cv::Mat::zeros(image.size(), CV_8UC1);
    std::vector<std::vector<cv::Point>> contours = { contour };
    cv::drawContours(mask, contours, -1, cv::Scalar(255), cv::FILLED);

    // Create an opaque version of the original image
    cv::Mat overlayedImage = image.clone();

    // Apply the mask to the overlayed image
    overlayedImage.setTo(contourColor, mask);

    // Blend the overlayed image with the original image
    double alpha = 0.4;
    cv::addWeighted(overlayedImage, alpha, image, 1.0 - alpha, 0, image);
    cv::drawContours(image, contours, -1, contourColor, 1 + ((image.rows + image.cols) / 400));
}

// Function to add a contour to the list if it's not a duplicate
void addContour(const std::vector<cv::Point>& contour) {
    // Check for duplicate contours
    if (std::find(contoursList.begin(), contoursList.end(), contour) == contoursList.end()) {
        contoursList.push_back(contour);
    }
}

int contourThickness(cv::Mat image) {
    int rows = image.rows;
    int cols = image.cols;

    int thickness = 2;

    thickness += (rows + cols) / 200;

    return thickness;
}




int getAverageHSV(const cv::Mat& image, int x, int y) {
    int rows = image.rows;
    int cols = image.cols;
    int scale = 2;

    if (x < scale) {
        x = scale;
    }
    if (y < scale) {
        y = scale;
    }
    if (x > rows - scale) {
        x = rows - scale;
    }
    if (y > cols - scale) {
        y = cols - scale;
    }
    //Should maybe make it dependant on the size of the image
    // Calculate the region of interest (ROI) in the middle of the image
    int startX = x - scale;
    int endX = x + scale;
    int startY = y - scale;
    int endY = y + scale;

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




std::vector<std::vector<cv::Point>> getContours(cv::Mat& image) {

    cv::Mat gray;
    cv::cvtColor(image, gray, cv::COLOR_BGR2GRAY);

    cv::Mat blurredImage;
    cv::GaussianBlur(gray, blurredImage, cv::Size(1, 1), 0, 0);

    // Apply Canny edge detection
    cv::Mat edges;
    cv::Canny(blurredImage, edges, 50, 135);

    // Apply dilation to enhance edges
    cv::Mat dilatedEdges;
    cv::dilate(edges, dilatedEdges, cv::Mat(), cv::Point(-1, -1), 2 + ((image.rows + image.cols) / 1500));

    // Find contours in the mask
    std::vector<std::vector<cv::Point>> contours;
    cv::findContours(dilatedEdges, contours, cv::RETR_EXTERNAL, cv::CHAIN_APPROX_SIMPLE);

    std::vector<std::vector<cv::Point>> filteredContours;
    int minArea = 2000;

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

    int hsvValue = getAverageHSV(image, x, y);
    int h = (hsvValue >> 16) & 0xFF;
    int s = (hsvValue >> 8) & 0xFF;
    int v = hsvValue & 0xFF;

    std::vector<std::vector<cv::Point>> contours;
    if (s < 60) {
        contours = getContours(image);
    } else {
        contours = getContours(image);
    }

    // Check if the specific pixel is within any contour
    for (const auto& contour : contours) {
        if (cv::pointPolygonTest(contour, point, false) >= 0) {

            //cv::drawContours(image, std::vector<std::vector<cv::Point>>{contour}, -1, cv::Scalar(0, 255, 0), contourThickness(image));

            //Add contour to the list
            addContour(contour);
            cv::circle(image, point, 5, cv::Scalar(255, 0, 0), -1); // Draw the specific pixel
            break;
        }
    }

    // Draw the contours containing the specific pixels
    cv::drawContours(image, contoursList, -1, cv::Scalar(0, 255, 0), contourThickness(image));

    return image;
}

void clearContourList() {
    contoursList.clear();
}

// Function to remove the newest contour from the list (if not empty)
void removeNewestContour(cv::Mat image) {
    if (!contoursList.empty()) {
        contoursList.pop_back(); // Remove the most recently added contour
    }

    cv::drawContours(image, contoursList, -1, cv::Scalar(0, 255, 0), contourThickness(image));
}


int findArea() {
    double area = 0;

    //Calculate total area of all contours
    for (const auto& contour : contoursList) {
        area += cv::contourArea(contour);
    }

    //Clear the global contour list
    contoursList.clear();

    return area;
}

int findObjectArea(cv::Mat image, int x, int y) {
    // Create a point for the specific pixel
    cv::Point point(x, y);

    int hsvValue = getAverageHSV(image, x, y);
    int h = (hsvValue >> 16) & 0xFF;
    int s = (hsvValue >> 8) & 0xFF;
    int v = hsvValue & 0xFF;

    std::vector<std::vector<cv::Point>> contours;
    if (s < 50) {
        contours = getContours(image);
    }
    else {
        contours = getContours(image);
    }
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


void findObjectInfo(cv::Mat image, int x, int y) {
    // Create a point for the specific pixel
    cv::Point point(x, y);

    std::vector<std::vector<cv::Point>> contours = getContours(image);
    double area = 0;
    cv::Point center(0, 0);

    // Check if the specific pixel is within any contour
    for (const auto& contour : contours) {
        if (cv::pointPolygonTest(contour, point, false) >= 0) {

            // Calculate the area
            area = cv::contourArea(contour);

            // Draw the contour containing the specific pixel
            drawWeightedContour(image, contour);
            cv::circle(image, point, 5, cv::Scalar(0, 0, 255), -1); // Draw the specific pixel

            // Calculate center
            center = cv::Point(0, 0);
            for (const auto& p : contour) {
                center += p;
            }
            center.x /= contour.size();
            center.y /= contour.size();

            areaInfo = area;
            imageInfo = image;
            centerInfo = center;

            break;
        }
    }
}

void centerObjectInfo(cv::Mat image) {

    std::vector<std::vector<cv::Point>> contours = getContours(image);
    double area = 0;
    cv::Point center(0, 0);

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
            center.x = mu[i].m10 / mu[i].m00;
            center.y = mu[i].m01 / mu[i].m00;
            area = cv::contourArea(contours[i]);
        }
    }

    // Draw the contour of the center object onto the image
    drawWeightedContour(image, contours[centerContourIndex]);

    areaInfo = area;
    imageInfo = image;
    centerInfo = center;
}



extern "C"
JNIEXPORT void JNICALL
Java_com_example_blodpool_MainActivity_cvTest(JNIEnv *env, jobject thiz, jlong mat_addy, jlong mat_addy_res, jint x_addy, jint y_addy) {

//cv::cvtColor(matIn, matOut, cv::COLOR_BGR2GRAY);
cv::Mat &mat = *(cv::Mat*) mat_addy;

cv::Mat &resMat = *(cv::Mat*) mat_addy_res;

cv::rotate(mat, mat, cv::ROTATE_90_CLOCKWISE);
cv::rotate(resMat, resMat, cv::ROTATE_90_CLOCKWISE);

int x = static_cast<int>(x_addy);
int y = static_cast<int>(y_addy);

resMat = findObject(mat, x, y);

//resMat = getEdges(mat);
}

extern "C"
JNIEXPORT jint JNICALL
Java_com_example_blodpool_MainActivity_findArea(JNIEnv *env, jobject thiz, jlong mat_addy, jint x_addy, jint y_addy) {

    cv::Mat &mat = *(cv::Mat*) mat_addy;

    cv::rotate(mat, mat, cv::ROTATE_90_CLOCKWISE);

    int x = static_cast<int>(x_addy);
    int y = static_cast<int>(y_addy);

    int pixels = findObjectArea(mat, x, y);

    return pixels;
}

extern "C"
JNIEXPORT void JNICALL
Java_com_example_blodpool_MainActivity_Undo(JNIEnv *env, jobject thiz, jlong mat_addy) {

    cv::Mat &mat = *(cv::Mat*) mat_addy;

    cv::rotate(mat, mat, cv::ROTATE_90_CLOCKWISE);

    removeNewestContour(mat);
}

extern "C"
JNIEXPORT void JNICALL
Java_com_example_blodpool_MainActivity_rotateMat(JNIEnv *env, jobject thiz, jlong mat_addy, jlong mat_addy_res) {

    //cv::cvtColor(matIn, matOut, cv::COLOR_BGR2GRAY);
    cv::Mat &mat = *(cv::Mat*) mat_addy;

    cv::Mat &resMat = *(cv::Mat*) mat_addy_res;

    cv::rotate(mat, resMat, cv::ROTATE_90_CLOCKWISE);

}


extern "C"
JNIEXPORT void JNICALL
Java_com_example_blodpool_MainActivity_removeAllContours(JNIEnv *env, jobject thiz) {

    clearContourList();

}

extern "C" JNIEXPORT jint JNICALL
Java_com_example_blodpool_MainActivity_findAreaTwo(JNIEnv *env, jobject thiz) {

    return findArea();
}

extern "C" JNIEXPORT void JNICALL
Java_com_example_blodpool_MainActivity_findobjectinfo(JNIEnv *env, jobject thiz, jlong mat_addy, jint x_addy, jint y_addy) {
    int x = static_cast<int>(x_addy);
    int y = static_cast<int>(y_addy);
    cv::Mat &mat = *(cv::Mat*) mat_addy;

    findObjectInfo(mat, x, y);
}

extern "C" JNIEXPORT void JNICALL
Java_com_example_blodpool_MainActivity_centerobjectinfo(JNIEnv *env, jobject thiz, jlong mat_addy) {
    cv::Mat &mat = *(cv::Mat*) mat_addy;

    centerObjectInfo(mat);
}


extern "C" JNIEXPORT void JNICALL
Java_com_example_blodpool_MainActivity_getimage(JNIEnv *env, jobject thiz, jlong mat_addy) {
    cv::Mat &mat = *(cv::Mat*) mat_addy;

    mat = getImage();
}


int main() {


    return 0;
}