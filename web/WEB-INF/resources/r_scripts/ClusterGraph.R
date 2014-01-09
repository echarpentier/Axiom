args <- commandArgs(TRUE)
callsFile <- args[1]
probeInfosFile <- args[2]
outputFile <- args[3]
subtitle1 <- args[4]
subtitle2 <- args[5]
plateColor <- args[6]
showModels <- args[7]
showPosteriors <- args[8]

calls <- read.table(callsFile, sep="\t", h=T)
probes <- read.table(probeInfosFile, sep="\t", h=T)
plateColorBoolean <- as.logical(plateColor)
showModelsBoolean <- as.logical(showModels)
showPosteriorsBoolean <- as.logical(showPosteriors)

if (showModelsBoolean | showPosteriorsBoolean){
	library(mixtools)
}

pdf(file=outputFile, title="Cluster Graph")
snps <- unique(calls$affyProbeName)
dd <- c("red","green3","blue","cyan","magenta","yellow","gray","chocolate4","darkgreen","orangered","deeppink3","cornflowerblue","coral3","darkviolet","tan1","slategray4","purple3","forestgreen","royalblue","olivedrab","mistyrose2","lightpink2","lightgoldenrod","khaki4","gold4","darkred")
palette(dd)


for ( snp in snps){
	
		probe <- head(probes[probes$affyProbeName==snp,], n=1)
		snp_mat <- calls[calls$affyProbeName==snp,]
		plot(snp_mat$logRatio,snp_mat$strength,xlab="Log Ratio",ylab="Strength", col="white", main=paste(snp," / ",probe$rsName))
		mtext(subtitle1, side=3)
		mtext(subtitle2, side=4)
	
		adnTag <- with(snp_mat, grepl("ADN", snp_mat$sampleName))
		refTag <- with(snp_mat, grepl("REF", snp_mat$sampleName))
		tag103 <- with(snp_mat, grepl("103", snp_mat$sampleName))

		if(plateColorBoolean){
			plateNames <- unique(snp_mat$plateName)
			plateNamesLegend <- c()
			plateColors <- c()
			i <- 0
			for ( plateName in plateNames){
				i <- i+1

				if (length(snp_mat$logRatio[snp_mat$affyCall=="AA" & snp_mat$plateName==plateName & !((adnTag & tag103) | (refTag & tag103))])!=0){
					matplot(snp_mat$logRatio[snp_mat$affyCall=="AA" & snp_mat$plateName==plateName & !((adnTag & tag103) | (refTag & tag103))],snp_mat$strength[snp_mat$affyCall=="AA" & snp_mat$plateName==plateName & !((adnTag & tag103) | (refTag & tag103))],add=T,pch=15,col=i, cex=0.8)
				}
				if (length(snp_mat$logRatio[snp_mat$affyCall=="AB" & snp_mat$plateName==plateName & !((adnTag & tag103) | (refTag & tag103))])!=0){
					matplot(snp_mat$logRatio[snp_mat$affyCall=="AB" & snp_mat$plateName==plateName & !((adnTag & tag103) | (refTag & tag103))],snp_mat$strength[snp_mat$affyCall=="AB" & snp_mat$plateName==plateName & !((adnTag & tag103) | (refTag & tag103))],add=T,pch=16,col=i, cex=0.8)
				}
				if (length(snp_mat$logRatio[snp_mat$affyCall=="BB" & snp_mat$plateName==plateName & !((adnTag & tag103) | (refTag & tag103))])!=0){
					matplot(snp_mat$logRatio[snp_mat$affyCall=="BB" & snp_mat$plateName==plateName & !((adnTag & tag103) | (refTag & tag103))],snp_mat$strength[snp_mat$affyCall=="BB" & snp_mat$plateName==plateName & !((adnTag & tag103) | (refTag & tag103))],add=T,pch=17,col=i, cex=0.8)
				}
				if (length(snp_mat$logRatio[snp_mat$affyCall=="NC" & snp_mat$plateName==plateName & !((adnTag & tag103) | (refTag & tag103))])!=0){
					matplot(snp_mat$logRatio[snp_mat$affyCall=="NC" & snp_mat$plateName==plateName & !((adnTag & tag103) | (refTag & tag103))],snp_mat$strength[snp_mat$affyCall=="NC" & snp_mat$plateName==plateName & !((adnTag & tag103) | (refTag & tag103))],add=T,pch=4,col=i, cex=0.8)
				}
				plateNamesLegend[i] <- toString(head(snp_mat$plateName[snp_mat$plateName==plateName], n=1))
				plateColors[i] <- i
			
				matplot(snp_mat$logRatio[((adnTag & tag103) | (refTag & tag103)) & snp_mat$plateName==plateName],snp_mat$strength[((adnTag & tag103) | (refTag & tag103)) & snp_mat$plateName==plateName],add=T,pch=8,col=i,cex=0.8)
			
			}
		
			legend("topright", plateNamesLegend, col=plateColors, cex=0.5, pch=18, pt.cex=1)
			#legend("topleft", c(paste("AA ","(", head(snp_mat$alleles[snp_mat$affyCall=="AA"], n=1),")"),paste("AB ","(", head(snp_mat$alleles[snp_mat$affyCall=="AB"], n=1),")"),paste("BB ","(", head(snp_mat$alleles[snp_mat$affyCall=="BB"], n=1),")"),"NC","ADN103",paste("fqA(HapMap)=",probe$freqAlleleA),paste("fqB(HapMap)=",probe$freqAlleleB)), cex=0.5, col=c("black","black","black","black","black","white","white"), pch=c(15,16,17,4,8,1,1))
			
			legend("topleft", c(paste("AA ","(",probe$alleleA,probe$alleleA,")",sep=""), paste("AB ","(", probe$alleleA, probe$alleleB, ")",sep=""), paste("BB ","(", probe$alleleB, probe$alleleB, ")",sep=""), "NC", "ADN103", paste("fqA(HapMap)=",probe$freqAlleleA), paste("fqB(HapMap)=",probe$freqAlleleB)), cex=0.5, col=c("black","black","black","black","black","white","white"), pch=c(15,16,17,4,8,1,1))


		}else{
	
			if (length(snp_mat$logRatio[snp_mat$affyCall=="AA" & !((adnTag & tag103) | (refTag & tag103))])!=0){
				matplot(snp_mat$logRatio[snp_mat$affyCall=="AA" & !((adnTag & tag103) | (refTag & tag103))],snp_mat$strength[snp_mat$affyCall=="AA" & !((adnTag & tag103) | (refTag & tag103))],add=T,pch=15,col="blue", cex=0.8)
			}
			if (length(snp_mat$logRatio[snp_mat$affyCall=="AB" & !((adnTag & tag103) | (refTag & tag103))])!=0){
				matplot(snp_mat$logRatio[snp_mat$affyCall=="AB" & !((adnTag & tag103) | (refTag & tag103))],snp_mat$strength[snp_mat$affyCall=="AB" & !((adnTag & tag103) | (refTag & tag103))],add=T,pch=16,col="red", cex=0.8)
			}
			if (length(snp_mat$logRatio[snp_mat$affyCall=="BB" & !((adnTag & tag103) | (refTag & tag103))])!=0){
				matplot(snp_mat$logRatio[snp_mat$affyCall=="BB" & !((adnTag & tag103) | (refTag & tag103))],snp_mat$strength[snp_mat$affyCall=="BB" & !((adnTag & tag103) | (refTag & tag103))],add=T,pch=17,col="green", cex=0.8)
			}
			if (length(snp_mat$logRatio[snp_mat$affyCall=="NC" & !((adnTag & tag103) | (refTag & tag103))])!=0){
				matplot(snp_mat$logRatio[snp_mat$affyCall=="NC" & !((adnTag & tag103) | (refTag & tag103))],snp_mat$strength[snp_mat$affyCall=="NC" & !((adnTag & tag103) | (refTag & tag103))],add=T,pch=4,col="black", cex=0.8)
			}
			
			matplot(snp_mat$logRatio[snp_mat$affyCall=="AA" & ((adnTag & tag103) | (refTag & tag103))],snp_mat$strength[snp_mat$affyCall=="AA" & ((adnTag & tag103) | (refTag & tag103))],add=T,pch=8,col="blue", cex=0.8)
			matplot(snp_mat$logRatio[snp_mat$affyCall=="AB" & ((adnTag & tag103) | (refTag & tag103))],snp_mat$strength[snp_mat$affyCall=="AB" & ((adnTag & tag103) | (refTag & tag103))],add=T,pch=8,col="red", cex=0.8)
			matplot(snp_mat$logRatio[snp_mat$affyCall=="BB" & ((adnTag & tag103) | (refTag & tag103))],snp_mat$strength[snp_mat$affyCall=="BB" & ((adnTag & tag103) | (refTag & tag103))],add=T,pch=8,col="green", cex=0.8)
			matplot(snp_mat$logRatio[snp_mat$affyCall=="NC" & ((adnTag & tag103) | (refTag & tag103))],snp_mat$strength[snp_mat$affyCall=="NC" & ((adnTag & tag103) | (refTag & tag103))],add=T,pch=8,col="black", cex=0.8)

			#legend("topright", c(paste("AA ","(", unique(snp_mat$alleles[snp_mat$affyCall=="AA"]),")"),paste("AB ","(", unique(snp_mat$alleles[snp_mat$affyCall=="AB"]),")"),paste("BB ","(", unique(snp_mat$alleles[snp_mat$affyCall=="BB"]),")"),"NC","ADN103",paste("fqA(HapMap)=",probe$freqAlleleA),paste("fqB(HapMap)=",probe$freqAlleleB)), cex=0.5, col=c("blue","red","green","black","black","white","white"), pch=c(15,16,17,4,8))
		
			legend("topleft", c(paste("AA ","(",probe$alleleA,probe$alleleA,")",sep=""), paste("AB ","(", probe$alleleA, probe$alleleB, ")",sep=""), paste("BB ","(", probe$alleleB, probe$alleleB, ")",sep=""), "NC", "ADN103", paste("fqA(HapMap)=",probe$freqAlleleA), paste("fqB(HapMap)=",probe$freqAlleleB)), cex=0.5, col=c("blue","red","green","black","black","white","white"), pch=c(15,16,17,4,8))

		}

		if (showModelsBoolean){
			priorsTag <- with(probes, grepl("priors", probes$modelName))
			priors <- probes[probes$affyProbeName==snp & priorsTag,]
			
			priorsLty <- c()
			priorsName <- c()
			
			for (i in 1:nrow(priors)){
				prior <- priors[i, ]
				priorsLty[i] <- i+1
				priorsName[i] <- toString(prior$modelName)
				mu<-c(prior$BBmeanX, prior$BBmeanY)
				sigma<-matrix(c(prior$BBvarX,prior$BBcovarXY,prior$BBcovarXY,prior$BBvarY), 2, 2)
				ellipse(mu, sigma, npoints = 100, newplot = FALSE, draw = TRUE, lty=i+1)

				mu<-c(prior$ABmeanX, prior$ABmeanY)
				sigma<-matrix(c(prior$ABvarX,prior$ABcovarXY,prior$ABcovarXY,prior$ABvarY), 2, 2)
				ellipse(mu, sigma, npoints = 100, newplot = FALSE, draw = TRUE, lty=i+1)

				mu<-c(prior$AAmeanX, prior$AAmeanY)
				sigma<-matrix(c(prior$AAvarX,prior$AAcovarXY,prior$AAcovarXY,prior$AAvarY), 2, 2)
				ellipse(mu, sigma, npoints = 100, newplot = FALSE, draw = TRUE, lty=i+1)
			}
		}

		if (showPosteriorsBoolean){
			post <- probes[probes$affyProbeName==snp & probes$modelName=="posteriors",]

			mu<-c(post$BBmeanX, post$BBmeanY)
			sigma<-matrix(c(post$BBvarX,post$BBcovarXY,post$BBcovarXY,post$BBvarY), 2, 2)
			ellipse(mu, sigma, npoints = 100, newplot = FALSE, draw = TRUE)

			mu<-c(post$ABmeanX, post$ABmeanY)
			sigma<-matrix(c(post$ABvarX,post$ABcovarXY,post$ABcovarXY,post$ABvarY), 2, 2)
			ellipse(mu, sigma, npoints = 100, newplot = FALSE, draw = TRUE)

			mu<-c(post$AAmeanX, post$AAmeanY)
			sigma<-matrix(c(post$AAvarX,post$AAcovarXY,post$AAcovarXY,post$AAvarY), 2, 2)
			ellipse(mu, sigma, npoints = 100, newplot = FALSE, draw = TRUE)
		}

		if (showModelsBoolean & showPosteriorsBoolean){
			legend("bottomright", c(priorsName,"posteriors"), lty=c(priorsLty,1), cex=0.5, col="black")
		} else if(showModelsBoolean){
			legend("bottomright", priorsName, lty=priorsLty, cex=0.5, col="black")
		} else if(showPosteriorsBoolean){
			legend("bottomright", "posteriors", lty=1, cex=0.5, col="black")
		}
}

