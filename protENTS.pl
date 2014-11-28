#!/usr/bin/perl -w


$dt=$ARGV[0];
$rank=$ARGV[1];
$min=$ARGV[2];

open(DT, "<$dt");
while(<DT>) {
	chomp($_);
	if(/^\d+\s+px\s+(\S+\.\d+)\.\d+\.\d+\s+(\S+)/) {
		$id = $2;
		$fold = $1;
                $folds{$id} = $fold;
		$agg{$1} = 0.0;
		$count{$1} = 0.0;
                
	}
}
close(DT);

$total=1.0;
$sum=0.0;
$sumq=0.0;
open(RANK, "<$rank");
while(<RANK>) {
	chomp($_);
	if(/^(\S+)\s+(\S+)/) {
		$id=$1; $score=$2;
		
		if($id=~/^g(\S+)/) {
			$id="d".$1;
		}

		#print "HAHA1: $score\n";
		#if($score==0.0) {
		#	last;
		#}
		#elsif($score>0.0 && $score != 1.0 && defined $folds{$id}) {
		if($score > $min && $score != 1.0 && defined $folds{$id}) {

		#if(defined $folds{$id}) {
			$sum+=$score;
			$sumq+=$score*$score;
			$total+=1.0;
        		my $fold=$folds{$id};
                	$agg{$fold}+=$score; 
			$count{$fold}+=1.0;
			#print "HAHA2: $sum\t$total\n";
		}
	}
}
close(RANK);

$mean = $sum/$total;
$std = $sumq/$total-$mean*$mean;
#print "$mean\t$std\n";

foreach $fold (sort {$agg{$b}<=>$agg{$a}} keys %agg) {
	if($count{$fold}>0.0 && $agg{$fold}>0.0) {
		$fmean{$fold} = $agg{$fold}/$count{$fold};
		$fstd{$fold} = $std*($total-$count{$fold})/$count{$fold}/($total-1); 
		$fz{$fold} = ($fmean{$fold}-$mean)/sqrt($fstd{$fold});
	}
}

foreach $fold (sort {$fz{$b}<=>$fz{$a}} keys %fz) {
	
	print "$fold\t$fz{$fold}\t$fmean{$fold}\t$agg{$fold}\t$count{$fold}\t$fstd{$fold}\n";
}
