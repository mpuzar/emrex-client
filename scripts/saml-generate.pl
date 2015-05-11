#!/usr/bin/perl

use strict;

my ($id, $sso, $slo, $cert);

if (@ARGV < 3) {
	print "Usage: $0 <entity_id> <url_sso> <url_slo> [certificate_filename]\n\n";
	
	print "Enter the entity ID (example: idporten-studentweb-utv-w3utv):         ";
	$id = <STDIN>;
	print "Enter the login return URL (example: https://localhost/samlsso.jsf):  ";
	$sso = <STDIN>;
	print "Enter the logout return URL (example: https://localhost/samlsso.jsf): ";
	$slo = <STDIN>;
} else {
   ($id, $sso, $slo) = @ARGV;
}

$id =~ s/[\s\n]+//;
$sso =~ s/[\s\n]+//;
$slo =~ s/[\s\n]+//;

my $tpl = getTpl();
$tpl =~ s/{URL_SSO}/$sso/g;
$tpl =~ s/{URL_SLO}/$slo/g;
$tpl =~ s/{ID}/$id/g;

if (@ARGV < 4) {
   print STDERR "Paste the certificate now.\n";
   $cert = "";
   while (defined(my $line = <STDIN>)) {
      $cert .= $line;
   }
   
} else {
   local $/;
   open IN, $ARGV[3] or die "Cannot open file $ARGV[3].";
   $cert = <IN>;
   close IN;
}

$cert =~ s/-+(BEGIN|END) CERTIFICATE-+[\n\s]+//gm;
$cert =~ s/(^\n+|\n+$)//g;

$tpl =~ s/{CERT}/$cert/g;

print $tpl;

sub getTpl {
    return <<TPL;
<?xml version="1.0" encoding="UTF-8"?>
<md:EntityDescriptor xmlns:md="urn:oasis:names:tc:SAML:2.0:metadata" entityID="{ID}">
   <md:SPSSODescriptor AuthnRequestsSigned="true" WantAssertionsSigned="true" protocolSupportEnumeration="urn:oasis:names:tc:SAML:2.0:protocol">
      <md:KeyDescriptor use="signing">
         <ds:KeyInfo xmlns:ds="http://www.w3.org/2000/09/xmldsig#">
            <ds:X509Data>
               <ds:X509Certificate>
{CERT}
               </ds:X509Certificate>
            </ds:X509Data>
         </ds:KeyInfo>
      </md:KeyDescriptor>
      <md:KeyDescriptor use="encryption">
         <ds:KeyInfo xmlns:ds="http://www.w3.org/2000/09/xmldsig#">
            <ds:X509Data>
               <ds:X509Certificate>
{CERT}
               </ds:X509Certificate>
            </ds:X509Data>
         </ds:KeyInfo>
      </md:KeyDescriptor>
      <md:AssertionConsumerService Binding="urn:oasis:names:tc:SAML:2.0:bindings:HTTP-Artifact" Location="{URL_SSO}" index="1" isDefault="true"/>
      <md:SingleLogoutService Binding="urn:oasis:names:tc:SAML:2.0:bindings:HTTP-Redirect" Location="{URL_SLO}"/>
      <md:NameIDFormat>urn:oasis:names:tc:SAML:2.0:nameid-format:transient</md:NameIDFormat>
      <md:NameIDFormat>urn:oasis:names:tc:SAML:2.0:nameid-format:persistent</md:NameIDFormat>
   </md:SPSSODescriptor>
</md:EntityDescriptor>
TPL
}
