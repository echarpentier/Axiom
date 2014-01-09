args <- commandArgs(TRUE)
corrFile <- args[1]
outputFile <- args[2]

corrs <- read.table(corrFile, sep="\t", h=T, stringsAsFactors=F)

genoNames <- as.character(corrs$genoName)

for (i in 1:(length(genoNames))){
	corr <- corrs[corrs$genoName==genoNames[i],]
	reportx<-read.table(file=corr$reportFile,header=T,sep="\t")
	corrx<-read.table(file=corr$corrFile,header=T,sep="\t")
	rpx<-merge(reportx,corrx,by.x="cel_files",by.y="sampleNewName",all=F,sort=T)
	if (i==1){
		rp <- rpx
	}else{
		rp <- rbind(rp,rpx)
	}
}

pdf(file=outputFile)
par(font.axis=2,col.axis="black",col.lab="darkgrey",font.lab=2,mai=c(2.8,1,1,1),pty="m")

#het_rate
boxplot(rp$het_rate ~ rp$plateName,main="het_rate",xlab="Plates",ylab="het_rate", border="blue", col="lightblue", las=2, outline=F);
medList<-tapply(X=rp$het_rate, INDEX=list(rp$plateName), FUN=median)
med<-median(medList)
upperSD<-med+(2*sd(medList))
lowerSD<-med-(2*sd(medList))
abline(h=med,col="darkgreen")
abline(h=upperSD,col="darkgreen",lty="dashed")
abline(h=lowerSD,col="darkgreen",lty="dashed")

#hom_rate
boxplot(rp$hom_rate ~ rp$plateName,main="hom_rate",xlab="Plates",ylab="hom_rate", border="blue", col="lightblue", las=2,outline=F);
medList<-tapply(X=rp$hom_rate, INDEX=list(rp$plateName), FUN=median)
med<-median(medList)
upperSD<-med+(2*sd(medList))
lowerSD<-med-(2*sd(medList))
abline(h=med,col="darkgreen")
abline(h=upperSD,col="darkgreen",lty="dashed")
abline(h=lowerSD,col="darkgreen",lty="dashed")

#call_rate
boxplot(rp$call_rate ~ rp$plateName,main="call_rate",xlab="Plates",ylab="call_rate", border="blue", col="lightblue", las=2,outline=F);
medList<-tapply(X=rp$call_rate, INDEX=list(rp$plateName), FUN=median)
med<-median(medList)
upperSD<-med+(2*sd(medList))
lowerSD<-med-(2*sd(medList))
abline(h=med,col="darkgreen")
abline(h=upperSD,col="darkgreen",lty="dashed")
abline(h=lowerSD,col="darkgreen",lty="dashed")

#cluster_distance_mean
boxplot(rp$cluster_distance_mean ~ rp$plateName,main="cluster_distance_mean",xlab="Plates",ylab="cluster_distance_mean", border="blue", col="lightblue", las=2,outline=F);
medList<-tapply(X=rp$cluster_distance_mean, INDEX=list(rp$plateName), FUN=median)
med<-median(medList)
upperSD<-med+(2*sd(medList))
lowerSD<-med-(2*sd(medList))
abline(h=med,col="darkgreen")
abline(h=upperSD,col="darkgreen",lty="dashed")
abline(h=lowerSD,col="darkgreen",lty="dashed")

#cluster_distance_stdev
boxplot(rp$cluster_distance_stdev ~ rp$plateName,main="cluster_distance_stdev",xlab="Plates",ylab="cluster_distance_stdev", border="blue", col="lightblue", las=2,outline=F);
medList<-tapply(X=rp$cluster_distance_stdev, INDEX=list(rp$plateName), FUN=median)
med<-median(medList)
upperSD<-med+(2*sd(medList))
lowerSD<-med-(2*sd(medList))
abline(h=med,col="darkgreen")
abline(h=upperSD,col="darkgreen",lty="dashed")
abline(h=lowerSD,col="darkgreen",lty="dashed")

#allele_summarization_mean
boxplot(rp$allele_summarization_mean ~ rp$plateName,main="allele_summarization_mean",xlab="Plates",ylab="allele_summarization_mean", border="blue", col="lightblue", las=2,outline=F);
medList<-tapply(X=rp$allele_summarization_mean, INDEX=list(rp$plateName), FUN=median)
med<-median(medList)
upperSD<-med+(2*sd(medList))
lowerSD<-med-(2*sd(medList))
abline(h=med,col="darkgreen")
abline(h=upperSD,col="darkgreen",lty="dashed")
abline(h=lowerSD,col="darkgreen",lty="dashed")

#allele_summarization_stdev
boxplot(rp$allele_summarization_stdev ~ rp$plateName,main="allele_summarization_stdev",xlab="Plates",ylab="allele_summarization_stdev", border="blue", col="lightblue", las=2,outline=F);
medList<-tapply(X=rp$allele_summarization_stdev, INDEX=list(rp$plateName), FUN=median)
med<-median(medList)
upperSD<-med+(2*sd(medList))
lowerSD<-med-(2*sd(medList))
abline(h=med,col="darkgreen")
abline(h=upperSD,col="darkgreen",lty="dashed")
abline(h=lowerSD,col="darkgreen",lty="dashed")

#allele_deviation_mean
boxplot(rp$allele_deviation_mean ~ rp$plateName,main="allele_deviation_mean",xlab="Plates",ylab="allele_deviation_mean", border="blue", col="lightblue", las=2,outline=F);
medList<-tapply(X=rp$allele_deviation_mean, INDEX=list(rp$plateName), FUN=median)
med<-median(medList)
upperSD<-med+(2*sd(medList))
lowerSD<-med-(2*sd(medList))
abline(h=med,col="darkgreen")
abline(h=upperSD,col="darkgreen",lty="dashed")
abline(h=lowerSD,col="darkgreen",lty="dashed")

#allele_deviation_stdev
boxplot(rp$allele_deviation_stdev ~ rp$plateName,main="allele_deviation_stdev",xlab="Plates",ylab="allele_deviation_stdev", border="blue", col="lightblue", las=2,outline=F);
medList<-tapply(X=rp$allele_deviation_stdev, INDEX=list(rp$plateName), FUN=median)
med<-median(medList)
upperSD<-med+(2*sd(medList))
lowerSD<-med-(2*sd(medList))
abline(h=med,col="darkgreen")
abline(h=upperSD,col="darkgreen",lty="dashed")
abline(h=lowerSD,col="darkgreen",lty="dashed")

#allele_mad_residuals_mean
boxplot(rp$allele_mad_residuals_mean ~ rp$plateName,main="allele_mad_residuals_mean",xlab="Plates",ylab="allele_mad_residuals_mean", border="blue", col="lightblue", las=2,outline=F);
medList<-tapply(X=rp$allele_mad_residuals_mean, INDEX=list(rp$plateName), FUN=median)
med<-median(medList)
upperSD<-med+(2*sd(medList))
lowerSD<-med-(2*sd(medList))
abline(h=med,col="darkgreen")
abline(h=upperSD,col="darkgreen",lty="dashed")
abline(h=lowerSD,col="darkgreen",lty="dashed")

#allele_mad_residuals_stdev
boxplot(rp$allele_mad_residuals_stdev ~ rp$plateName,main="allele_mad_residuals_stdev",xlab="Plates",ylab="allele_mad_residuals_stdev", border="blue", col="lightblue", las=2,outline=F);
medList<-tapply(X=rp$allele_mad_residuals_stdev, INDEX=list(rp$plateName), FUN=median)
med<-median(medList)
upperSD<-med+(2*sd(medList))
lowerSD<-med-(2*sd(medList))
abline(h=med,col="darkgreen")
abline(h=upperSD,col="darkgreen",lty="dashed")
abline(h=lowerSD,col="darkgreen",lty="dashed")

