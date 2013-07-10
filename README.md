Stgn
=========

Stgn is yet another tool for [steganography](http://en.wikipedia.org/wiki/Steganography). Stgn hides a file into image by modifying __specified__ bits of image pixels.

Abstract
---------

Digital image consist of [pixels](http://en.wikipedia.org/wiki/Pixel). Each pixel may be represented as a combination of 3 base colors: red, green and blue. This way of representation is called [RGB](http://en.wikipedia.org/wiki/RGB) model. These 3 colors are also called "channels". Another channel may be used to control image transparency, it is called [alpha channel](http://en.wikipedia.org/wiki/Alpha_channel). If alpha channel is used together with RGB values we have [ARGB](http://en.wikipedia.org/wiki/RGBA_color_space) image. Colour and transpatrency of each channel (ARGB values) may be represented by an integer number in range 0..255. 8 bits (1 byte) is enough to store integer number in range 0..255. In this case we have 8-bit ARGB image. For example we may represent purple pixel as the following 32-bit ARGB binary number: 

    00000000 11111111 00000000 11111111

It was noticed that modification of least significant bits of any channel is not recognized by human eye. It means we can put 8 bit (1 byte) of secret information inside a pixel without noticeable visual effect. Lets assume our secret byte is (letter __X__ in [ASCII](http://en.wikipedia.org/wiki/Ascii) character table):

    01011000

If we modify 2 least significant bits of our original image using bits from our secret byte we will have:

    00000001 11111101 00000010 11111100
    ------** ------** ------** ------**


Which will be negligibly more trasparent, less red, more green and less blue. Modified bits are marked with ```*```. If we use ```1``` to mark bits we want to modify and ```0``` to mark bits we keep intact from the previous example we will have the following [Bit Mask](http://en.wikipedia.org/wiki/Bit_mask):

    00000011 00000011 00000011 00000011

 which may be shorlty represented as [hexadecimal number](http://en.wikipedia.org/wiki/Hexadecimal_number):

    03 03 03 03

Stgn allows to fully control which bits to modify by setting any Bit Mask. For example if you set hexadecimal

    00 07 00 03

 which in binary form looks like

    00000000 00000111 00000000 00000011

And it means that no modifications will be done in alpha and green channel, three bits will be saved in red and two bits in blue channel. Since you have 5 bits available for modification in this case, more than 1 pixel is required to store 1 byte of secret.
Reading bits specified by bit mask and combining them into bytes allows us to get previously stored secret from image pixels.


Definitions
----------

**container** - source image, javax.imageio readable image file (jpg,png,gif,bmp)

**secret** - an existing file, we want to hide into container

**bitmask** - bits for each ARGB pixel used to hide secret info. default: 0x03030303

**conceal** - to hide secret into container by modifying bits specified by bitmask

**unveil** - extract previously concealed secret by reading bits specified by bitmask


Contents
-----------

**Stgn.java** - main library

**Stgnc.java** - provides command line interface

**StgnGUI.java** - provides simple GUI interface

**Test.java** - test routines

**Util.java** - a few simple wrappers for Array and File manipulations

**BitArrayInputStream.java** - wrapper for bit manipulations

**BitArrayOutputStream.java** - wrapper for bit manipulations

**build.xml** - ant build file

**ant_makeall.bat** - batch file to run ant build process

**run_test.bat** - batch file to run test application

**run_console.bat** - batch file to run console application

**run_gui.bat** - batch file to run GUI application

**StgnGUI.jnlp** - java web start sample file

**container.png** - sample image file

**secret.jpg** - sample image file

How to build
------------

The easiest way is to run:

    ant makeall

which is equivalent to:

    ant clean compile makejar genkey signjar


How to run
------------

There are three options:

1. using **ant**:

    ```ant runtest``` - to run end-to-end test

    ```ant runconsole``` - to run console application

    ```ant rungui``` - to run GUI application

2. using provided ***.bat** files:

    ```run_test.bat``` which runs: ```java -cp jar/lud.stgn.jar lud.stgn.Test```

    ```run_console.bat``` which runs: ```java -cp jar/lud.stgn.jar lud.stgn.Stgnc --conceal secret.jpg --into container.png```

    ```run_gui.bat``` which runs:   ```java -cp jar/lud.stgn.jar lud.stgn.StgnGUI```

3. using java web start and provided ***.jnlp** file:
    
    double click ```StgnGUI.jnlp``` or try something like ```javaws StgnGUI.jnlp```

Usage
-----------
Make sure your container is big enough to conceal your secret.
The following formula is used to calculate container **capacity**:

    C = W * H * b / 8
    
    Where:
        C - container capacity in bytes
        W - container image width in pixels
        H - container image height in pixels
        b - number of bits set to 1 in your bitmask

Use the following options to conceal:

```Stgnc --conceal secret.info --into container.png [ --bitmask 0x03030303 ] [ --saveas ihaveasecret.png ]```

    secret.info   - an existing file, we want to hide into container
    container.png - javax.imageio readable image file (jpg/png/gif/bmp)
    bitmask       - hex integer, bits for each ARGB pixel used to hide secret info. default: 0x03030303
    saveas        - file to be created. foramt is always 8bit ARGB png. default: ihaveasecret.png

In order to unveil the secret you have toknow both **bitmask** previously used to conceal and **size** of unveiled secret (number of bytes) to get from image.

Use the following options to unveil:

```Stgnc --unveil  hidden.info --from ihaveasecret.png --size 100000 [ --bitmask 0x03030303 ]```

    hidden.info      - resulting file, contains extracted info from ihaveasecret.png
    ihaveasecret.png - existing file with hidden info inside
    size             - number of bytes to extract from ihaveasecret
    bitmask          - bits for each ARGB pixel used to hide secret info. deafult: 0x03030303

