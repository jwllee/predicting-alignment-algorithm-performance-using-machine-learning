DIR=$(shell pwd)
DATADIR=$(shell dirname "$(shell pwd)")/prediction-data

run: 
	sudo docker run --rm -p 8889:8888 -e JUPYTER_ENABLE_LAB=yes -v $(DIR):/home/jovyan/work -v $(DATADIR):/home/jovyan/data jupyter/datascience-notebook:9b06df75e445

build:
	sudo docker build --tag=predict-alignment .

bash: build
	sudo docker run -it -v $(DIR):/home/jovyan/work -v $(DATADIR):/home/jovyan/data predict-alignment /bin/bash

