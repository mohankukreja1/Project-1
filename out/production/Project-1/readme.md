## CSE 565 Project-1 Http Server

1. 200 Response-

    1.1 curl "127.0.0.1:4000/html/text.html" -v (Html File)
    1.2 curl "127.0.0.1:4000/css/sample.css" -v (CSS File)
    1.3 curl "127.0.0.1:4000/gif/giphy.gif" -v (Gif File)
    1.4 curl "127.0.0.1:4000/jpeg/image.jpeg" -v (Jpeg File)
    1.5 curl "127.0.0.1:4000/mp4/video1.mp4" -v (Mp4 File)
    1.6 curl "127.0.0.1:4000/png/array.png" -v (Png File)
    1.7 curl "127.0.0.1:4000/text/text.txt" -v (Text File)
    1.8 curl "127.0.0.1:4000/webm/sample.webm" -v (Webm File)

2. 206 Response -

   1.1 curl -r 0-3 "127.0.0.1:4000/html/text.html" -v (Html File)

   Works for all of the above requests

3. 404 Response

   1.1 curl "127.0.0.1:4000/html/text5.html" -v (Html File)

4. Browser Test

   1.1 http://127.0.0.1:4000/mp4/video1.mp4

5. Starting Server

   cd into the Project-1/src directory and run ./Makefile.sh 3000
   3000 represents the port number. You can specify any random port