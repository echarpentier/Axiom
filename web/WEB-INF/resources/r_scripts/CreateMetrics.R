#setwd("/BELENOS/data/user/echarpentier/Axiom/Analyses/output");
#getwd();
args<-commandArgs(TRUE)
posteriorsFile<-args[1]
callsFile<-args[2]
annotFile<-args[3]
outputFile<-args[4]

#Read in the posterior file#
#This section will read in the posterior information from a genotyping run. This will only work if the “—write-models” option has been used to save the posteriors when processing the files#

print("Importing posteriors",quote=F)
#posteriors<-read.table("/BELENOS/data/user/echarpentier/Axiom/Analyses/geno_all_2/AxiomGT1.snp-posteriors.txt.gz",header=T,dec=".",stringsAsFactors=F);
posteriors<-read.table(file=posteriorsFile,header=T,dec=".",stringsAsFactors=F)

#Read in the calls file#
#This code will read in the results from the calls file from an APT genotyping run.#

print("Importing calls",quote=F)
#calls<-read.table("/BELENOS/data/user/echarpentier/Axiom/Analyses/geno_all_2/AxiomGT1.calls.txt.gz",header=T,sep="\t",stringsAsFactors=F);
calls<-read.table(file=callsFile,header=T,sep="\t",stringsAsFactors=F)

print("Importing annotations",quote=F)
#calls<-read.table("/BELENOS/data/user/echarpentier/Axiom/Analyses/geno_all_2/AxiomGT1.calls.txt.gz",header=T,sep="\t",stringsAsFactors=F);
annot<-read.table(file=annotFile,header=T,sep="\t",stringsAsFactors=F)

#Clean up the posteriors and calls#
#This section cleans up the posteriors and the calls for further processing#

print("Clean up posteriors and calls",quote=F)
posteriors.1<-posteriors[posteriors$id %in% calls$probeset_id,]
calls.1<-calls[calls$probeset_id %in% posteriors$id,]
posteriors.1<-posteriors.1[order(posteriors.1$id,posteriors.1$BB),]
calls.1<-calls.1[order(calls.1$probeset_id,-calls.1[,2]),]
posteriors.1<-posteriors.1[!duplicated(posteriors.1$id),]
calls.1<-calls.1[!duplicated(calls.1$probeset_id),]

#Parse the posteriors#
#This section of the code will parse posterior values into individual values for later analysis#

print("Parse posteriors",quote=F)
BB<-unlist(strsplit(posteriors.1$BB,","))
BB.meanX<-BB[seq(1,length(BB),7)]
BB.varX<-BB[seq(2,length(BB),7)]
BB.nObsMean<-BB[seq(3,length(BB),7)]
BB.nObsVar<-BB[seq(4,length(BB),7)]
BB.meanY<-BB[seq(5,length(BB),7)]
BB.varY<-BB[seq(6,length(BB),7)]
BB.covarXY<-BB[seq(7,length(BB),7)]
AB<-unlist(strsplit(posteriors.1$AB,","))
AB.meanX<-AB[seq(1,length(AB),7)]
AB.varX<-AB[seq(2,length(AB),7)]
AB.nObsMean<-AB[seq(3,length(AB),7)]
AB.nObsVar<-AB[seq(4,length(AB),7)]
AB.meanY<-AB[seq(5,length(AB),7)]
AB.varY<-AB[seq(6,length(AB),7)]
AB.covarXY<-AB[seq(7,length(AB),7)]
AA<-unlist(strsplit(posteriors.1$AA,","))
AA.meanX<-AA[seq(1,length(AA),7)]
AA.varX<-AA[seq(2,length(AA),7)]
AA.nObsMean<-AA[seq(3,length(AA),7)]
AA.nObsVar<-AA[seq(4,length(AA),7)]
AA.meanY<-AA[seq(5,length(AA),7)]
AA.varY<-AA[seq(6,length(AA),7)]
AA.covarXY<-AA[seq(7,length(AA),7)]
posteriors.2<-data.frame(id=posteriors.1$id,BB.meanX,BB.varX,BB.nObsMean,BB.nObsVar,BB.meanY,BB.varY,BB.covarXY,AB.meanX,AB.varX,AB.nObsMean,AB.nObsVar,AB.meanY,AB.varY,AB.covarXY,AA.meanX,AA.varX,AA.nObsMean,AA.nObsVar,AA.meanY,AA.varY,AA.covarXY,stringsAsFactors=F)

#count genotype call#
#This section counts the number of each type of genotype call#

print("Count genotype call",quote=F)
calls.c0<-apply(calls.1[,2:ncol(calls.1)],1,function(x){length(x[x==0])})
calls.c1<-apply(calls.1[,2:ncol(calls.1)],1,function(x){length(x[x==1])})
calls.c2<-apply(calls.1[,2:ncol(calls.1)],1,function(x){length(x[x==2])})
calls.c3<-apply(calls.1[,2:ncol(calls.1)],1,function(x){length(x[x==-1])})
calls.2<-data.frame(probeset_id=calls.1$probeset_id,"AA"=calls.c0,"AB"=calls.c1,"BB"=calls.c2,"NN"=calls.c3,stringsAsFactors=F)

#FLD calculation step 1#
#Prepare for fld value calculation#

print("FLD calculation",quote=F)
fld.1<-(as.numeric(posteriors.2$AA.meanX)-as.numeric(posteriors.2$AB.meanX))/sqrt(as.numeric(posteriors.2$AB.varX))
fld.2<-(as.numeric(posteriors.2$AB.meanX)-as.numeric(posteriors.2$BB.meanX))/sqrt(as.numeric(posteriors.2$AB.varX))
posteriors.3<-data.frame(id=posteriors.2$id,"fld.AA.AB"=fld.1,"fld.AB.BB"=fld.2,stringsAsFactors=F)

#FLD calculation step 2#
#Determine which fld value to assign based on configuration of populated clusters#

findfld<-function(x){
if(x[1]>0 & x[2]>0 & x[3]>0) return(4)
if(x[1]>0 & x[2]>0 & x[3]==0) return(1)
if(x[1]>0 & x[2]==0 & x[3]>0) return(0)
if(x[1]==0 & x[2]>0 & x[3]>0) return(2)
if(x[1]>0 & x[2]==0 & x[3]==0) return(0)
if(x[1]==0 & x[2]>0 & x[3]==0) return(0)
if(x[1]==0 & x[2]==0 & x[3]>0) return(0)
if(x[1]==0 & x[2]==0 & x[3]==0) return(0)
}
whichfld<-apply(calls.2[,2:5],1,findfld)
calls.2<-cbind(calls.2,whichfld,stringsAsFactors=F)
posteriors.3<-cbind(posteriors.3,fld=-1,stringsAsFactors=F)
if(nrow(calls.2[calls.2$whichfld==4,])>0)posteriors.3[posteriors.3$id %in% calls.2[calls.2$whichfld==4,1],]$fld<-apply(posteriors.3[posteriors.3$id %in% calls.2[calls.2$whichfld==4,1],2:3],1,min)
if(nrow(calls.2[calls.2$whichfld==1,])>0)posteriors.3[posteriors.3$id %in% calls.2[calls.2$whichfld==1,1],]$fld<-posteriors.3[posteriors.3$id %in% calls.2[calls.2$whichfld==1,1],2]
if(nrow(calls.2[calls.2$whichfld==2,])>0)posteriors.3[posteriors.3$id %in% calls.2[calls.2$whichfld==2,1],]$fld<-posteriors.3[posteriors.3$id %in% calls.2[calls.2$whichfld==2,1],3]

#Calculate homRO#

print("homRO calculation",quote=F)
homRO<-data.frame(id=posteriors.3$id,"homROa"=rep(100,nrow(posteriors.3)),"homROb"=rep(-100,nrow(posteriors.3)),"homRO"=rep(0,nrow(posteriors.3)),stringsAsFactors=F) 
homRO[homRO$id %in% calls.2[calls.2$BB>0,1],"homROb"]<-posteriors.2[posteriors.2$id %in% calls.2[calls.2$BB>0,1],"BB.meanX"]
homRO[homRO$id %in% calls.2[calls.2$AA>0,1],"homROa"]<-posteriors.2[posteriors.2$id %in% calls.2[calls.2$AA>0,1],"AA.meanX"] 
homRO$homRO<-apply(homRO[,2:3],1,function(x){min(abs(as.numeric(x[1])),abs(as.numeric(x[2])))}) 
homRO[as.numeric(homRO$homROa)<0,"homRO"]<-homRO[as.numeric(homRO$homROa)<0,"homROa"]
homRO[as.numeric(homRO$homROb)>0,"homRO"]<--1*as.numeric(homRO[as.numeric(homRO$homROb)>0,"homROb"])
homRO[as.numeric(homRO$homRO)==100,"homRO"]<--10

#Cluster counting#
#This section calcuates the number of clusters#

print("Cluster counting",quote=F)
findnc<-function(x){
if(x[1]>0 & x[2]>0 & x[3]>0) return(3)
if(x[1]>0 & x[2]>0 & x[3]==0) return(2)
if(x[1]>0 & x[2]==0 & x[3]>0) return(2)
if(x[1]==0 & x[2]>0 & x[3]>0) return(2)
if(x[1]>0 & x[2]==0 & x[3]==0) return(1)
if(x[1]==0 & x[2]>0 & x[3]==0) return(1)
if(x[1]==0 & x[2]==0 & x[3]>0) return(1)
if(x[1]==0 & x[2]==0 & x[3]==0) return(0)
}
nc<-apply(calls.2[,2:5],1,findnc)
calls.2<-cbind(calls.2,nc,stringsAsFactors=F)

#hetSO#
#This section calculates hetSO values for the SNPs#

print("hetSO calculation",quote=F)
hetSO<-data.frame(id=posteriors.3$id,"ye"=rep(0,nrow(posteriors.3)),"hetSO"=rep(0,nrow(posteriors.3)),stringsAsFactors=F)
ye<-apply(posteriors.2,1,function(x){as.numeric(x[6])+(as.numeric(x[20])-as.numeric(x[6]))*((as.numeric(x[9])-as.numeric(x[2]))/(as.numeric(x[16])-as.numeric(x[2])))})
hetSO$ye<-ye
ye.1<-as.numeric(posteriors.2$AB.meanY)-ye
hetSO$hetSO<-ye.1
hetSO[hetSO$id %in% calls.2[calls.2$AB==0,1],3]<--10

#SNP call rate#
#This section calculates the SNP call rate (CR)#

print("SNP call rate",quote=F)
cr<-(calls.2$AA+calls.2$AB+calls.2$BB)/(calls.2$AA+calls.2$AB+calls.2$BB+calls.2$NN)*100

#HomFLD#
#This section calculates the HomFLD (fld value for the homozygous calls#

print("HomFLD calculation",quote=F)
HomFLD<-data.frame(id=posteriors.3$id,"HomFLD"=rep(-1,nrow(posteriors.3)),stringsAsFactors=F)
HomFLD$HomFLD<-(as.numeric(posteriors.2$AA.meanX)-as.numeric(posteriors.2$BB.meanX))/sqrt(as.numeric(posteriors.2$AB.varX))
HomFLD[HomFLD$id %in% calls.2[calls.2$AA==0 | calls.2$BB==0,1],2]<--1

#Minor allele count#
#This section of code calculates the nMinorAllele#

print("Minor Allele count",quote=F)
countMinorAllele<-function(x){
if(which.max(x)==1) return(x[2]+x[3])
if(which.max(x)==2) return(x[1]+x[3])
if(which.max(x)==3) return(x[1]+x[2])
}
calls.2$nMinorAllele<-apply(calls.2[,2:4],1,countMinorAllele)

#SNP pNclus2#
#This section calculates the probability of having 2 clusters instead of 3 knowing the minor allele frequency and number of samples observed#
calls.2<-merge(calls.2,annot,by.x="probeset_id",by.y="probesetID")
countSamples<-function(x){
return(x[1]+x[2]+x[3]+x[4])
}
mycols <- c("AA","AB","BB","NN")
nSamples<-apply(calls.2[1,which(names(calls.2) %in% mycols)],1,countSamples)
mycols <- c("freqAlleleA","freqAlleleB")
findMAF<-function(x){
return(min(as.numeric(x[1]),as.numeric(x[2])))
}
calls.2$maf <- apply(calls.2[,which(names(calls.2) %in% mycols)],1,findMAF)
#mycols <- c("maf")
pb <- function(x){
return(pbinom(q=0,size=nSamples,p=as.numeric((x[1]))^2,lower.tail=T))
}
calls.2$pNClus2 <- lapply(calls.2[,"maf"],pb)

#Write the output file#
#This section of code writes out the metric table#

print("Writing output",quote=F)
write.table(data.frame("probeset_id"=posteriors.3$id,CR=cr,"FLD"=posteriors.3$fld,HomFLD=HomFLD$HomFLD,"HetSO"=hetSO$hetSO,"HomRO"=homRO$homRO,"nMinorAllele"=calls.2$nMinorAllele,"Nclus"=calls.2$nc,"maf"=calls.2$maf,"pNClus2"=signif(as.numeric(calls.2$pNClus2),digits = 3),"n_AA"=calls.2$AA,"n_AB"=calls.2$AB,"n_BB"=calls.2$BB,"n_NC"=calls.2$NN),outputFile,quote=F,sep="\t",row.names=F)

#Finding your output#
#This command will indicate the directory containing the “metrics.txt” file#

print("DONE",quote=F)
getwd()

