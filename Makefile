JAVATOCXX_PATH := /usr/local/include/javatocxx
default:clean build

clean:
	rm -rf build/

build:
	gradle build

install:
	rm -rf $(JAVATOCXX_PATH) 
	mkdir $(JAVATOCXX_PATH) 
	cp build/libs/JavaToCXX.jar ${JAVATOCXX_PATH} 
	cp j2c /usr/local/bin/
