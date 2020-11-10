#!/usr/bin/env bash

ipWIF=`ip a | grep "inet " | grep wlan0 | awk {'print $2'}| awk -F / {'print $1'}`
macblu=`hcitool dev | tail -1 | awk {'print $2'}`
macWIF=`ip a | grep "wlan0" -A 2 | head -n 2 | tail -n 1 | awk {'print $2'}`
if [ "x$(ip a|grep eth0)" != "x" ];then
macETH=`ip a | grep "eth0" -A 2 | head -n 2 | tail -n 1 | awk {'print $2'}`
ipETH=`ip a | grep "inet " | grep eth0 | awk {'print $2'}| awk -F / {'print $1'}`
fi
ser=`head -1 /etc/hostapd/hostapd.conf | sed s/[^0-9]//g  | sed 's/^[0]*//'`
verKalug=`vk=$(grep sc_version /var/www/html/sc_version.txt | awk -F "=" {'print $2'}); if [ -z $vk ]; then echo "Unknown"; else echo $vk; fi`
verBoard=`vb=$(grep stm_hrdw /var/www/html/sc_version.txt | awk -F "=" {'print $2'}); if [ -z $vb ]; then echo "Unknown"; else echo $vb; fi`
verFRSTM=`vf=$(grep stm_sftw /var/www/html/sc_version.txt | awk -F "=" {'print $2'}); if [ -z $vf ]; then echo "Unknown"; else echo $vf; fi`
verBLSTM=`vl=$(grep stm_load /var/www/html/sc_version.txt | awk -F "=" {'print $2'}); if [ -z $vl ]; then echo "Unknown"; else echo $vl; fi`
verCore=`cat /etc/stp-release`
compileDate=`ls -lh /etc/stp-release | awk {'print $6 " " $7 " " $8 '}`
day=`uptime | grep day`
if [ "x$(uptime | grep day)" == "x" ]; then
timer=`uptime | awk {'print $3'} | sed 's/,//g'`
Uptime=$timer
else
timer=`uptime | awk {'print $5'}`
dayer=`uptime | awk {'print $3'}| sed 's/,//g'`
dday="days"
Uptime=`echo "${dayer} ${dday} ${timer}"`
fi
load=`uptime | sed s/min,//g | awk {'print $7'} | sed s/,//g`
tempCPU=`t=$(cat /sys/class/thermal/thermal_zone0/temp); tempC=$(($t/1000)); echo "$tempC"`
typeT=`grep informer_type /etc/sc_uart/config.txt| awk -F "=" {'print $2'}`
if [ "$typeT" == "stationary" ]; then
crcline=`grep "crc" /var/www/html/data/text_files/stat_ru.json | awk {'print $2'} | sed 's/",//g' | sed 's/"//g'`
fi
modem=`grep "/usr/bin/pon" /etc/init.d/S99stp-tools | awk {'print $2'} | awk -F "-" {'print $1'}`
incrCONT=`cat /var/www/html/data/text_files/*ru.json  | grep incr | awk {'print $2'} | sed 's/"//g; s/,//g'`
echo -e "${ser}\n$ipWIF\n$macWIF\n$macblu\n$verBoard\n$verKalug\n$verFRSTM\n$verBLSTM\n$verCore\n$modem\n$incrCONT\n$Uptime\n$tempCPU\n$load\n$ipETH\n$macETH\n$typeT"
exit
