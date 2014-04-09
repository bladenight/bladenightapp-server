use DateTime;
use DateTime::Format::Strptime;
use Data::Dumper;
use DateTime::Format::ISO8601;

use strict;

my $startDate  = '2014-03-31T21:00:00';

my $fullFormat = DateTime::Format::Strptime->new(
    pattern => '%Y-%m-%dT%T'
);

my $onlyDateFormat = DateTime::Format::Strptime->new(
    pattern => '%Y-%m-%d'
);

opendir(my $dh, "../routes") || die "can't opendir: $!";
my @routeNames = map { s/\.kml// ; $_ } grep { /.*\.kml/ && ! /Familie/i } readdir($dh);
closedir $dh;

# print Dumper(\@routeNames);
    
my $dateTime   = $fullFormat->parse_datetime($startDate);
my $routeIndex = 0;

sub writeEvent {
  my ($event) = @_;
  my $dateStr = $fullFormat->format_datetime($event->{dateTime});
  my $filename = ( $onlyDateFormat->format_datetime($event->{dateTime}) ) . ".per";
  print "Writing to $filename\n";  
  open FH, ">$filename";
  print FH <<END;
{
  "startDate": "${dateStr}.000+02:00",
  "duration": 120,
  "routeName": "$event->{route}",
  "participants": $event->{participants},
  "status": "$event->{status}"
}
END
  close FH;
}

foreach my $i (1..15) {
  my $event = {};

  $event->{dateTime} = $dateTime;
  $event->{route} = $routeNames[$routeIndex];
  $event->{status} = "P";
  $event->{participants} = 0;
  
  if ( DateTime->compare(DateTime->now(), $dateTime) >= 0 ) {
    $event->{status} = ( int(rand(2)) ? "O" : "A");
    $event->{participants} = ( $event->{status} eq "O" ? 1000 + 100 * int(rand(50)) : 0 );
  }

  writeEvent($event);

  $dateTime->add(days => 7);
  $routeIndex = ( $routeIndex + 1 ) % scalar(@routeNames);
}

if ( 0 ) {
  my $event;
  $event->{dateTime} = DateTime->now()->add(hours => 2);
  $event->{route} = $routeNames[0];
  $event->{status} = "O";
  $event->{participants} = 0;
  writeEvent($event);
}

