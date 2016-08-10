/**Wallet.java
 * @作者         吕浪
 * @创建于 2016-8-8 上午10:37:51
 * @版本    1.0
 */
package my;
import javacard.framework.APDU;
import javacard.framework.Applet;
import javacard.framework.ISO7816;
import javacard.framework.ISOException;
import javacard.framework.Util;

public class Wallet extends Applet {
	private short balance;//余额,最大为7FFF(十六进制表示)
	private MyPin myPin;
	//short是两个byte字节
	
	Wallet()
	{
		myPin = new MyPin();
		balance = (short)0;
	}
	
	public static void install(byte[] bArray, short bOffset, byte bLength) {
		// GP-compliant JavaCard applet registration
		new Wallet().register(bArray, (short) (bOffset + 1), bArray[bOffset]);
	}
	//验证pin
	public void checkPin(APDU apdu)
	{
		if(myPin.pinBlock())
			ISOException.throwIt((short)0x6283);
		byte[] buf = apdu.getBuffer();
		apdu.setIncomingAndReceive();
		byte tryCounter = myPin.check(buf, ISO7816.OFFSET_CDATA, buf[ISO7816.OFFSET_LC]);
		if(tryCounter == (byte)2)
			ISOException.throwIt((short)0x63C2);
		else if(tryCounter == (byte)1)
			ISOException.throwIt((short)0x63C1);
		else if(tryCounter == (byte)0)
			ISOException.throwIt((short)0x63C0);
	}
	//修改pin
	public void setNewPin(APDU apdu)
	{
		if(myPin.pinBlock())
			ISOException.throwIt((short)0x6283);
		if(!myPin.pinVerify()) //未验证pin
			ISOException.throwIt((short)0x6982);
		byte[] buf = apdu.getBuffer();
		apdu.setIncomingAndReceive();
		myPin.setPin(buf, ISO7816.OFFSET_CDATA, buf[ISO7816.OFFSET_LC]);
	}
	//获取余额
	public void getBalance(APDU apdu)
	{
		if(myPin.pinBlock())
			ISOException.throwIt((short)0x6283);
		if(!myPin.pinVerify())
			ISOException.throwIt(ISO7816.SW_SECURITY_STATUS_NOT_SATISFIED);
		byte[] buf = apdu.getBuffer();
		
		//关键函数,将short表示的十进制数转化(拆分)成十六进制的两byte的字节数组
		Util.setShort(buf, (short)ISO7816.OFFSET_CDATA, balance);
		
		apdu.setOutgoingAndSend(ISO7816.OFFSET_CDATA, (short)2);
	}
	
	//加上余额
	public void increase(byte[] buf, short bufOffset)
	{
		short temp = Util.getShort(buf, (short)ISO7816.OFFSET_CDATA);//将2字节的数组表示的十六进制数转化成short十进制数
		if(temp < 0)//传了负数进来报异常
			ISOException.throwIt(ISO7816.SW_CONDITIONS_NOT_SATISFIED);
		temp += this.balance;
		if(temp < (short)0) //超额存储
		{
			ISOException.throwIt(ISO7816.SW_CONDITIONS_NOT_SATISFIED);
		}
		else
		{
			this.balance = temp;
		}
	}
	
	//减去余额
	public void decrease(byte[] buf, short bufOffset)
	{
		short temp = Util.getShort(buf, (short)ISO7816.OFFSET_CDATA);//将2字节的数组表示的十六进制数转化成short十进制数
		if(temp < 0)//传了负数进来报异常
			ISOException.throwIt(ISO7816.SW_CONDITIONS_NOT_SATISFIED);
		if(temp > this.balance) //超额消费
		{
			this.balance = (short)0; //超额则存最大值
			ISOException.throwIt(ISO7816.SW_CONDITIONS_NOT_SATISFIED); //然后抛异常说超限额
		}
		else
		{
			this.balance -= temp;
		}
	}
	//充值
	public void charge(APDU apdu)
	{
		if(myPin.pinBlock())
			ISOException.throwIt((short)0x6283);
		if(!myPin.pinVerify())
			ISOException.throwIt((short)0x6982);
		byte[] buf = apdu.getBuffer();
		apdu.setIncomingAndReceive();//这句记得别漏了,漏了会导致难以发现的bug!模拟器跑可能没问题但放到卡片跑程序就会出现奇怪的结果!
		if(buf[ISO7816.OFFSET_LC] != (byte)2) //必须用两字节输入操作金额
			ISOException.throwIt(ISO7816.SW_DATA_INVALID);
		increase(buf,(short)ISO7816.OFFSET_CDATA);
	}
	//消费
	public void consume(APDU apdu)
	{
		if(myPin.pinBlock())
			ISOException.throwIt((short)0x6283);
		if(!myPin.pinVerify())
			ISOException.throwIt((short)0x6982);
		byte[] buf = apdu.getBuffer();
		apdu.setIncomingAndReceive();
		if(buf[ISO7816.OFFSET_LC] != (byte)2)
			ISOException.throwIt(ISO7816.SW_DATA_INVALID);
		decrease(buf,(short)ISO7816.OFFSET_CDATA);
	}
	
	public void process(APDU apdu) {
		// Good practice: Return 9000 on SELECT
		if (selectingApplet()) {
			return;
		}
		
		//byte->short的转化实际是将一串0/1的二进制数转化成十进制数
		/*byte[] temp = {11,22};
		short bal = Util.getShort(temp, (short)0);
		Util.setShort(temp, (short)0, (short)1032);*/
		
		byte[] buf = apdu.getBuffer();
		switch (buf[ISO7816.OFFSET_INS]) {
		case (byte) 0x20:
			checkPin(apdu);
			break;
		case (byte) 0x21:
			setNewPin(apdu);
			break;
		case (byte) 0x10:
			getBalance(apdu);
			break;
		case (byte) 0x11:
			charge(apdu);
			break;
		case (byte) 0x12:
			consume(apdu);
			break;
		default:
			// good practice: If you don't know the INStruction, say so:
			ISOException.throwIt(ISO7816.SW_INS_NOT_SUPPORTED);
		}
	}
}

/**
 * 特别注意点：
 * apdu.setIncomingAndReceive();如果漏了这句命令，可能会导致无法获取缓冲区的data域，或者获取到的都是0.
 * 所以在process()中如果某种case要用到data域的，则必须传递一个APDU对象过去给处理函数，
 * 除非处理函数中不用到data域（如switch只用到INS域）。
 * 而不能只传递一个缓冲区buf过去,因为往往不会再process中写apdu.setIncomingAndReceive();命令，
 * 这就容易导致上面提到问题：
 * 会导致难以发现的bug!模拟器跑可能没问题但放到卡片跑程序就会出现奇怪的结果!
 */
