args <- commandArgs(TRUE)
samplesFile <- args[1]
outputFile <- args[2]

samples<-read.table(samplesFile,header=TRUE,sep="\t")

#getting median of medians
medList<-tapply(X=samples$DQC, INDEX=list(samples$plateID), FUN=median)
med<-median(medList)

#getting 2 * standard deviation of median
upperSD<-med+(2*sd(medList))
lowerSD<-med-(2*sd(medList))


png(file=outputFile, width = 2000, height = 2000, res=200)

par(font.axis=2,col.axis="black",col.lab="darkgrey",font.lab=2,mai=c(2.8,1,1,1),pty="m") 
bx <- boxplot(samples$DQC ~ samples$plateName,main="Box plots of DQC values per plate",xlab="Plates",ylab="DQC",ylim=c(0.80,1.01),border="blue",col="lightblue",las=2)

#labeling outliers
#for(i in 1:length(bx$group)){
#text(bx$group[i],bx$out[i],samples$sampleName[which((samples$DQC==bx$out[i]) & (samples$plateName==bx$names[bx$group[i]]))],pos=2, offset=0.5, cex=0.8)
#}

#adding median, 2 stdev, lower dishQC lines
abline(h=med,col="darkgreen")
abline(h=upperSD,col="darkgreen",lty="dashed")
abline(h=lowerSD,col="darkgreen",lty="dashed")
abline(h=0.82,col="red")

dev.off()


