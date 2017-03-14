echo "Select demo to run:"
echo
echo " [1] DisplayMessages.Main"
echo " [2] SendMessageToDevice.Main"
echo
read -p "Enter number: " -r opt

cd samples-java

if [ "$opt" = "1" ]; then
  mvn clean compile exec:java -Dexec.mainClass="DisplayMessages.Main"
fi

if [ "$opt" = "2" ]; then
  mvn clean compile exec:java -Dexec.mainClass="SendMessageToDevice.Main"
fi
