args<-commandArgs(TRUE)
pedPath<-args[1]
pdfFile<-args[2]

library(kinship)
pedfile <- read.table(pedPath,header=T,sep="\t",stringsAsFactors=FALSE)
familyIDs <- unique(pedfile$famID)

pdf(file=pdfFile)
for(famID in familyIDs){
	ped <- pedigree(pedfile$indID[pedfile$famID==famID],pedfile$dadID[pedfile$famID==famID],pedfile$momID[pedfile$famID==famID],pedfile$sex[pedfile$famID==famID],pedfile$affected[pedfile$famID==famID])
	plot(ped)
	title(famID)
}
#ped <- pedigree(pedfile$indID,pedfile$dadID,pedfile$momID,pedfile$sex,pedfile$affected,pedfile$status)
#plot(ped)
