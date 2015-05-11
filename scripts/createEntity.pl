#!/usr/bin/perl
#
# createEntity.pl (by Matija)
# 
# Skriptet tar på stdin en liste av kolonner som man får når man selecter
# dem i Columns-oversikten og lager tilsvarende Hibernate-entitet.  Det ville
# vært fint om det støttet flere typer inputs, men foreløpig holder dette.

# Resultatet blir <Class>.java og evt. <Class>Id.java (hvis skriptet finner
# ut at klassen har en embedded ID) i samme directory det blir kjørt i.
#
# Resultatet mangler en del imports for Hibernate, men det fikser man lett
# i Eclipse, så jeg gadd ikke bruke tid på det nå. 
#
# Forventet input og eksempel fra Toad :
#   ColumnName  ID  Pk  Null  DataType           Default  Histogram  EncryptionAlg  Salt  Seq/Trigger
#   EMNEKODE     4   4     N  VARCHAR2 (12 Byte)	      None
# 
# Forventet input og eksemepl fra SQLDeveloper:
#   ColumnName  DataType           Null  ColNr
#   BRUKERNAVN  VARCHAR2(70 CHAR)  No        1
#
# Forventet input og eksemepl fra SQLDeveloper for et ID-felt:
#   ColumnName  Pk
#   BRUKERNAVN   1
# 
# Det brukes kun ColumnName, Pk, Null og DataType (med tilsv. lengde for VARCHAR),
# der den sistenevnte blir "oversatt" til Java-type etter følgende regler:
#   NUMBER -> Long/Float
#   *VARCHAR* -> String
#   DATE -> Date
#   BLOB -> byte[]

use strict;

my %mapping = (
  'NUMBER.*?,[^0]'			=> 'Float',
  'NUMBER(.*?,0)*'			=> 'Long',
  'VARCHAR2?( *\((\d+)[^\d]*\))?'	=> 'String',
  'DATE'				=> ['Date', 'java.util.Date'],
  'BLOB'				=> 'byte[]',
  'CLOB'				=> 'Clob'
);

my $javafile_tpl = q{package %PACKAGE%;
%IMPORTS%
import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.EmbeddedId;
import javax.persistence.Embeddable;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.sql.Clob;
import javax.validation.constraints.NotNull;
import org.hibernate.validator.constraints.Length;

@Entity
@Table(name = "%TABLE%")
public %CLASSHEAD% implements Serializable {
    private static final long serialVersionUID = 1L;
    %ID%
    %DECLARATIONS%
    public %CLASS%() { }
    %GETTERS%
    
    %IDCLASS%
}
};

my $getter_tpl = q{
    %ANNOT%
    public %TYPE% get%CAPSVAR%() {
        return %VAR%;
    }
    
    public void set%CAPSVAR%(%TYPE% %VAR%) {
        this.%VAR% = %VAR%;
    }
};

my $decl_tpl = q{
    @Column(name = "%UCORIGVAR%"%NULLABLE%%LENGTH%)
    private %TYPE% %VAR%;
};

if (@ARGV<2) {
  print "Must specify the package name and the class name\n";
  exit 1;
}
my ($package, $class) = @ARGV;
my $table = uc($class);
shift; shift;

my (@declarations, @declarations_id, %declarations_todo, @getters, @getters_id, %getters_todo, @imports, @imports_id, @ids, @allcols);

while (defined(my $line = <>)) {
  chomp($line);
  $line =~ s/^\s+|\s+$//g;
  $line =~ s/ {2,}/\t/g;
  
  if ($line =~ /^Column/) {
      next;
  }
  my ($col, $pk, $null, $type);
  
  # Telle antall tabs
  my $count = () = $line =~ /\t/g;
  
  # SQL Developer
  # EIERKODE    VARCHAR2(10 BYTE)    No        1
  if ($count == 3) {
      ($col, $type, $null, my $colnr) = split(/\t/, $line, 4);
      
  # SQL Developer - Primary key
  # EIERKODE    1
  } elsif ($count == 1) {
      ($col, $pk) = split(/\t/, $line, 2);
      push @ids, lc($col);
      next;
      
  # Toad
  # EIERKODE	1	1	N	VARCHAR2 (10 Byte)		None
  } elsif ($count == 6) {
      ($col, my $d1, $pk, $null, $type, my $d2) = split(/\t/, $line, 6);
      if ($pk>0) {
        push @ids, lc($col);
      }
      
  } else  {
print "FAIL count $count\n";
      next;
  }
  
  $col = lc($col);
  my $col_orig = $col;
  $col =~ s/_(.{1})/\U$1/g;
  
  push @allcols, $col;
  
  my $found = 0;
  keys %mapping; # needed to reset the hash before each()
  
  while (my ($keyx, $val) = each(%mapping)) {
    if ($type =~ /$keyx/) {
      $found = 1;
      my ($type) = ('','');
      my @annots;
      my $length = $2;
      
      if (ref($val) eq 'ARRAY') {
        $type = $val->[0];
        my $import = "import " . $val->[1] . ";\n";
        if (!$pk) {
          push @imports, $import;
        } else {
          push @imports_id, $import;
        }
      } else {
        $type = $val;
      }
      
      my $v = $decl_tpl;
      my $uccol = uc($col);
      $v =~ s/%VAR%/$col/mg;
      $v =~ s/%UCORIGVAR%/$col_orig/mg;
      $v =~ s/%TYPE%/$type/mg;
      if ($null =~ /^N/) {
        $v =~ s/%NULLABLE%/, nullable = false/mg;
        push @annots, "\@NotNull";
      } else {
        $v =~ s/%NULLABLE%//mg;
      }
      
      if (defined($length) && $length ne '') {
        $v =~ s/%LENGTH%/, length = $length/g;
        push @annots, "\@Length(max = $length)";
      } else {
        $v =~ s/%LENGTH%//;
      }
      
      my $g = $getter_tpl;
      my $capscol = ucfirst($col);
      my $annot = join("\n    ", @annots);#s/(^\n+|\n+$)//m;
      $g =~ s/%TYPE%/$type/mg;
      $g =~ s/%VAR%/$col/mg;
      $g =~ s/%CAPSVAR%/$capscol/mg;
      $g =~ s/%ANNOT%/$annot/mg;
      
      $getters_todo{$col} = $g;
      $declarations_todo{$col} = $v;

      last;
    }
  }
  if (!$found) {
    #print STDERR "ERROR: Could not parse column $col !\n";
  }
}


# Sjekk ID-felt og sett annotasjonene
my %params;
foreach my $id (@ids) {
   $id =~ s/_(.{1})/\U$1/g;
   $params{$id} = 1;
}

foreach my $col (@allcols) {
  # my %params = map { $_ => 1 } @ids;
  if (exists($params{$col})) {
    push @declarations_id, $declarations_todo{$col};
    push @getters_id, $getters_todo{$col};
  } else {
    push @declarations, $declarations_todo{$col};
    push @getters, $getters_todo{$col};
  }
}


my ($id, $idclass);
if (@ids == 1) {
  push @getters, @getters_id;
  push @imports, @imports_id;
  $id = join('', map( { $_ =~ s/Column(.*)/Id/; $_ } @declarations_id));
   
} elsif (@ids > 1) {
  $idclass = "Id"; #$class . "Id";
  $id = "\n    \@EmbeddedId\n    private $idclass id;";
  my $g = $getter_tpl;
  $g =~ s/%TYPE%/$idclass/mg;
  $g =~ s/%VAR%/id/mg;
  $g =~ s/%CAPSVAR%/Id/mg;
  $g =~ s/%ANNOT%//mg;
  push @getters, $g;
}

my $decl_all = join('', @declarations);
my $imp_all = join('', uniq(@imports));
my $get_all = join("\n",@getters);
my $javafile = $javafile_tpl;
$javafile =~ s/%PACKAGE%/$package/gm;
$javafile =~ s/%TABLE%/$table/gm;
$javafile =~ s/%CLASS%/$class/gm;
$javafile =~ s/%CLASSHEAD%/class $class/gm;
$javafile =~ s/%IMPORTS%/$imp_all/;
$javafile =~ s/%DECLARATIONS%/$decl_all/;
$javafile =~ s/%GETTERS%/$get_all/;
$javafile =~ s/%ID%/$id/;
$javafile =~ s/[ \t]+\n/\n/mg;
$javafile =~ s/\n{3,}/\n\n\n/mg;

if (@ids > 1) {
  my $decl_all = join('', @declarations_id);
  my $imp_all = join('',uniq(@imports_id));
  my $get_all = join("\n",@getters_id);
  my $idfile = $javafile_tpl;
  #$idfile =~ s/%PACKAGE%/$package/gm;
  $idfile =~ s/^.*?\@Entity/\@Embeddable/s;
  $idfile =~ s/\@Table.*?\n//;
  $idfile =~ s/%CLASS%/$idclass/gm;
  $idfile =~ s/%CLASSHEAD%/static class $idclass/gm;
  #$idfile =~ s/%IMPORTS%/$imp_all/;
  $idfile =~ s/%DECLARATIONS%/$decl_all/;
  $idfile =~ s/%GETTERS%/$get_all/;
  $idfile =~ s/%ID%//;
  $idfile =~ s/\s*%IDCLASS%\s*/\n/s;
  $idfile =~ s/\n/\n    /gs;
  
  $javafile =~ s/%IDCLASS%[\n\s]*/$idfile\n/s;

} else {
  $javafile =~ s/\s*%IDCLASS%\s*//sg;

}

createfile("$class.java", $javafile);


sub uniq {
  my %h;
  return grep { !$h{$_}++ } @_
}

sub createfile {
  my ($class, $contents) = @_;
  print "Generating $class (" . @allcols . " columns, " . @ids . " of which part of the ID)\n";
  open F, ">$class";
  print F $contents;
  close F;
}
