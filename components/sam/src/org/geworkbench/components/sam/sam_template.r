args <- commandArgs(TRUE)
userLib	<- args[1]
if (!is.na(userLib))  .libPaths(userLib)
a<-require(siggenes)
if(a==FALSE){
  source("http://bioconductor.org/biocLite.R")
  biocLite("siggenes")
}

library(siggenes)
outdir<-paste(samdir,"output", sep="")
dir.create(outdir,showWarnings = FALSE)
samoutput<-paste(samdir,"output/", sep="")
datafile<-paste(samdir, "data.txt", sep="")
clfile<-paste(samdir,"cl.txt",sep="")
deltafile<-paste(samdir,"delta_vec.txt",sep="")
permfile<-paste(samdir,"perm.txt",sep="")
unlogfile<-paste(samdir,"unlog.txt",sep="")

outdfile<-paste(samoutput,"outd.txt",sep="")
outdbarfile<-paste(samoutput,"outdbar.txt",sep="")
outpvaluefile<-paste(samoutput,"outpvalue.txt",sep="")
outfoldfile<-paste(samoutput,"outfold.txt",sep="")
outmatfdrfile<-paste(samoutput,"outmatfdr.txt",sep="")
donefile<-paste(samoutput,"done.txt",sep="")

data<-read.table(datafile, sep="\t")
cl<-scan(clfile)
delta_vec<-scan(deltafile)
perm<-scan(permfile)
unlogpre<-scan(unlogfile)
unlog<-as.logical(unlogpre)
sam.out<-sam(data,cl,control=samControl(delta=delta_vec), use.dm = FALSE, B=perm, R.unlog=unlog, rand=123)
write.table(sam.out@d,outdfile)
write.table(sam.out@d.bar,outdbarfile)
write.table(sam.out@p.value,outpvaluefile)
write.table(sam.out@fold,outfoldfile)
write.table(sam.out@mat.fdr[,c(1,3:5)],outmatfdrfile)
write.table(cl,donefile)
