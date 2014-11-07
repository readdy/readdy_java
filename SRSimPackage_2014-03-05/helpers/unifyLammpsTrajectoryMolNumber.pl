#!/usr/bin/perl -w
use strict "vars";
local $SIG{__WARN__} = sub { die $_[0] };


my $MaxSpecies = 0;
my @MaxAtoms;
my @TSdata;
my $TStime;


open(my $FILE, "$ARGV[0]");
scanMaxNumbers($FILE);
close($FILE);

open($FILE, "$ARGV[0]");
while( &transcribeNextTs($FILE) ) {printf "."};
close($FILE);




sub scanMaxNumbers {
  my ($F) = @_;
  
  my $remember = 0;
  my @countAtoms;
  
  while(<$F>)
      {
      my $line = $_;
      chomp($line);
      
      if ($line =~ m/ITEM: TIMESTEP/) 
         {
         $remember = 0;
         @countAtoms = ( );
         }
      
      
      if ($remember)
         {
         my @tags = split(/ /, $line);
         my $id = $tags[0];
         my $tp = $tags[1];
         
         if ($tp > $MaxSpecies) {$MaxSpecies = $tp;}
         
         # see if we found a new max for the species of type $tp
         $countAtoms[$tp] ++;
         if (not defined $MaxAtoms[$tp])        {$MaxAtoms[$tp] = 0;}
         if ($countAtoms[$tp] > $MaxAtoms[$tp]) {$MaxAtoms[$tp] = $countAtoms[$tp];}
         
         $TSdata[$id] = $line;
         }
      
      if ($line =~ m/ITEM: ATOMS/) {$remember = 1;}
      }
      
  return 0;
  }
  
  
sub transcribeNextTs {
  my ($F) = @_;
  
  my $remember = 0;
  my $numAllAtoms = 0;
  for (my $i=0 ; $i<=$MaxSpecies ; $i++)
      {
      if (not defined $MaxAtoms[$i]) {$MaxAtoms[$i]=0;}
      $numAllAtoms += $MaxAtoms[$i];
      }
  
  
  while(<$F>)
      {
      my $line = $_;
      chomp($line);
      
      # are we switching back from remember -> don't remember?
      if ($remember==1 and $line =~ m/ITEM: TIMESTEP/) 
         {
         $remember = 0;
         &writeAtomsAndReset();
         }
         
      # skip the number of atoms, replace my numAllAtoms!
      if ($line =~ m/ITEM: NUMBER OF ATOMS/) 
         {
         print("ITEM: NUMBER OF ATOMS\n");
         print("$numAllAtoms\n");
         <$F>; # skip the number
         next;
         }
      
      
      if (not $remember)
         {print "$line\n";}
      else
         {
         my @tags = split(/ /, $line);
         my $id = $tags[0];
         my $tp = $tags[1];
         
         # delete the id-tag from the molecule:
         $line =~ s/^\d+\ //;
         
         if (not defined $TSdata[$tp]) { $TSdata[$tp] = []; }   # start a new unnamed array
         push(@{$TSdata[$tp]}, $line);
         }
      
      if ($line =~ m/ITEM: ATOMS/) {$remember = 1;}
      }
      
  # write the last trajectory timestep:
  &writeAtomsAndReset();
      
  return 0;
  }
  
  
  
sub writeAtomsAndReset
  {
  my $cnt = 1;
  
  for (my $i=0 ; $i<=$MaxSpecies ; $i++)
      {
      if (not defined $MaxAtoms[$i]) {$MaxAtoms[$i]=0;}
      # print ("Blub $MaxAtoms[$i]\n");
      
      for (my $j=0 ; $j<$MaxAtoms[$i] ; $j++)
          {
          if (defined $TSdata[$i] and $j < @{$TSdata[$i]} )
             { 
             print ("$cnt $TSdata[$i][$j]\n"); 
             $cnt++;
             }
          else
             { 
             print ("$cnt $i  0.0 0.0 0.0  0.0\n"); 
             $cnt++;
             }
             
          }
      }

      
  @TSdata = ();
  }