#!/bin/sh

ipa=`ip a | grep "inet " | grep 10. | grep wlan0 | awk {'print $2'}| awk -F / {'print $1'}`
macblu=`hcitool dev | tail -1 | awk {'print $2'}`
macwif=`ip a | grep "wlan0" -A 2 | head -n 2 | tail -n 1 | awk {'print $2'}`
ser=`head -1 /etc/hostapd/hostapd.conf | sed s/[^0-9]//g  | sed 's/^[0]*//'`
verKalug=`head -1 /var/www/html/sc_version.txt | tail -1| awk -F "=" {'print $2'}`
if [ "x$verKalug"  == "x" ];then
verKalug=`head -1 /var/www/html/sc_version.txt`
fi
verBoard=`head -2 /var/www/html/sc_version.txt | tail -1| awk -F "=" {'print $2'}`
	if [ "x$verBoard"  == "x" ] || [ "x$verBoard"  == "x$verKalug" ];then
	verBoard="Unknown"
	fi

verFRSTM=`head -3 /var/www/html/sc_version.txt | tail -1| awk -F "=" {'print $2'}`
        if [ "x$verFRSTM"  == "x" ] || [ "x$verFRSTM"  == "x$verKalug" ];then
        verSTM="Unknown"
        fi

verBLSTM=`head -4 /var/www/html/sc_version.txt | tail -1| awk -F "=" {'print $2'}`
        if [ "x$verBLSTM"  == "x" ] || [ "x$verBLSTM"  == "x$verKalug" ] || [ "x$verBLSTM"  == "x$verFRSTM" ];then
        verLoadSTM="Unknown"
        fi

verProsh=`cat /etc/stp-release`
compileDate=`ls -lh /etc/stp-release | awk {'print $6 " " $7 " " $8 '}`

day=`uptime | grep day`
if [ "x$day" == "x" ]; then
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
boardT=$(grep informer_type /etc/sc_uart/config.txt | tail -1 | awk -F "=" {'print $2'})
if [ "$boardT" == "transport" ]; then
	boardT="Транспорт"
	else
	if [ "$boardT" == "stationary" ]; then
        boardT="Стационар"
	else
	boardT="Unknown"
	fi
fi
crcline=`grep "crc" /var/www/html/data/text_files/stat_ru.json | awk {'print $2'} | sed 's/",//g' | sed 's/"//g'`
modem=`grep "/usr/bin/pon" /etc/init.d/S99stp-tools | awk {'print $2'} | awk -F "-" {'print $1'}`
incrCONT=`cat /var/www/html/data/text_files/*ru.json  | grep incr | awk {'print $2'} | sed 's/"//g' | sed 's/,//g'`
echo -e "${ser}\n$ipa\n$macwif\n$macblu\n$verBoard\n$verKalug\n$verFRSTM\n$verBLSTM\n$verProsh\n$modem\n$incrCONT\n$Uptime\n$tempCPU\n$load"
exit