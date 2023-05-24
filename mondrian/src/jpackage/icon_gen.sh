#!/usr/bin/env sh

IMAGE=icon_1024x1024.png

mkdir Mondrian.iconset
sips -s format png -z 16 16     $IMAGE --out Mondrian.iconset/icon_16x16.png
sips -s format png -z 32 32     $IMAGE --out Mondrian.iconset/icon_16x16@2x.png
sips -s format png -z 32 32     $IMAGE --out Mondrian.iconset/icon_32x32.png
sips -s format png -z 64 64     $IMAGE --out Mondrian.iconset/icon_32x32@2x.png
sips -s format png -z 128 128   $IMAGE --out Mondrian.iconset/icon_128x128.png
sips -s format png -z 256 256   $IMAGE --out Mondrian.iconset/icon_128x128@2x.png
sips -s format png -z 256 256   $IMAGE --out Mondrian.iconset/icon_256x256.png
sips -s format png -z 512 512   $IMAGE --out Mondrian.iconset/icon_256x256@2x.png
sips -s format png -z 512 512   $IMAGE --out Mondrian.iconset/icon_512x512.png
cp $IMAGE Mondrian.iconset/icon_512x512@2x.png
iconutil -c icns Mondrian.iconset
rm -R Mondrian.iconset