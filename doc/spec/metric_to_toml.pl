use strict;
use Data::Dumper;
use Getopt::Long 'GetOptions';

# デフォルト値の設定
my $filterPlatform = 'Linux';

# オプションの処理
GetOptions(
  'platform=s' => \$filterPlatform
);

sub level {
    my ($s) = @_;
    return "Level0" if ($s eq 'Y');
    return "Level1" if ($s eq 'Y1');
    return "Level2" if ($s eq 'Y2');
    return "None";
}

print "platform = \"${filterPlatform}\"\n";
print "\n";

while(<>) {
    s/(\r\n|\r|\n)$//g;;
    my @csv = split(',', $_);
    my $platform = pop(@csv);
    my $commandLevel = shift(@csv);
    my $category = shift(@csv);
    my $metric = shift(@csv);
    my $id = shift(@csv);
    my $deviceFlag = shift(@csv);
    my $comment = join(",", @csv);
    if ($category eq '' or $category eq 'Category' or
        $platform ne $filterPlatform) {
        next;
    }

    print "[[metrics]]\n";
    print "\n";
    print "metricId = \"${id}\"\n";
    print "metricName = \"${metric}\"\n";
    print "category = \"${category}\"\n";
    if (my $level = level($commandLevel)) {
    print "commandLevel = \"${level}\" # None,Level0,Level1,Level2\n";
    }
    if ($deviceFlag eq 'Y') {
        print "deviceFlag = true\n";
    } else {
        print "deviceFlag = false\n";
    }
    print "comment = \"${comment}\"\n";
    print "\n";
}
