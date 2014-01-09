args <- commandArgs(TRUE)
inputFile <- args[1]
outputFile <- args[2]
graphTitle <- args[3]

data <- read.table(file=inputFile, sep="\t", h=T)
xAxis <- names(data)[2]
yAxis <- names(data)[3]
png(filename=outputFile, width = 600, height = 600)
plot(data[,2], data[,3], main=graphTitle, xlab=xAxis, ylab=yAxis)
