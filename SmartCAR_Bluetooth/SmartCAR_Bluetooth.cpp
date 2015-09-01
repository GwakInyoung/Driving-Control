// Do not remove the include below
#include "SmartCAR_Bluetooth.h"

MPU6050 accelgyro;
int16_t MPU6050_data[6];

union DATA_DB{
   int16_t value;
   unsigned char buff[2];
};

union DATA_DB MPU;

int Motor[6] = {22,23,24,25,4,5};
int data = 0, flag = 0;
int dealy_time = 400,RX_flag = 1,PWM_value1,PWM_value2;

unsigned char RX_buf2[7];
unsigned char TX_buff[5] = {0x76, 0x00, 0xF0, 0x00, 0xF0};
unsigned char TX_bufs[5] = {0x76, 0x00, 0x0F, 0x00, 0x0F};
char RX_buf[7],RX_ultra[17];
unsigned char TX_buf[7]={0x76,0x00,0x21,0x00,},TX_buf_ultra[17]={0x76,0x00,0x3C,0x00,},TX_buf_sensor[22] = {0x76,0x00,0x33,0x00,};
unsigned int ENCODER_CNT_L=0,ENCODER_CNT_R=0,Encoder_value_L,Encoder_value_R,sensor_flag1=0,sensor_flag2=0,Ultra_EN = 0,sensor_read = 0;
unsigned char Timer_flag = 0;
unsigned char program_flag =0;

//The setup function is called once at startup of the sketch
void setup()
{
	int z;
	DEBUG_BEGIN(115200);
	Serial.begin(115200);
	Serial1.begin(115200);
	for(z=0;z<6;z++)
	{
		pinMode(Motor[z],OUTPUT);
		digitalWrite(Motor[z],LOW);
	}
	Wire.begin();
	DEBUG_PRINTLN("Initializing I2C devices...");
	accelgyro.initialize();

	DEBUG_PRINTLN("Testing device connections...");
	DEBUG_PRINTLN(accelgyro.testConnection() ? "MPU6050 connection successful" : "MPU6050 connection failed");
	Timer1::set(200000,Timer1_ISR);
	Timer1::start();
}

// The loop function is called in an endless loop
void loop()
{
	int z;
	if(sensor_flag1 != sensor_flag2)
	{
		sensor_flag1 = sensor_flag2;
		DEBUG_PRINTLN(" sensor_flag1 : ");
		DEBUG_PRINTLN(sensor_flag1);
		DEBUG_PRINTLN(sensor_flag2);
		DEBUG_PRINTLN(Ultra_EN);
		if(sensor_flag1 & 0x100)
		{
			if(Ultra_EN == 0)
			{
				Ultra_EN = 1;
				DEBUG_PRINTLN("Ultrasonic Enable");
				Serial1.write(TX_buff,5);
			}
		}
		else
		{
			if(Ultra_EN)
			{
				Ultra_EN = 0;
				DEBUG_PRINTLN("Ultrasonic Disable");
				Serial1.write(TX_bufs,5);
				for(z=4;z<16;z++)
				{
					TX_buf_ultra[z] = 0;
				}
				TX_buf_ultra[16] = TX_buf_ultra[2];
				Serial.write(TX_buf_ultra,17);
			}
		}
		for(z=4;z<21;z++)
		{
			TX_buf_sensor[z] = 0;
		}
		if(sensor_flag1 & 0xFF)
		{
			sensor_read = 1;
		}
		else if(sensor_read != 0)
		{
			sensor_read = 0;
			TX_buf_sensor[21] = TX_buf_sensor[2];
			Serial.write(TX_buf_sensor,22);
		}
		if(sensor_flag1 & 0x02)
		{
			attachInterrupt(6,Encoder_count_L,RISING);
			attachInterrupt(7,Encoder_count_R,RISING);
		}
		else
		{
			detachInterrupt(6);
			detachInterrupt(7);
		}
	}
	if(sensor_read & Timer_flag)
	{
		Timer_flag = 0;
		if(sensor_flag1 & 0xFC)
		{
			accelgyro.getMotion6(&MPU6050_data[0], &MPU6050_data[1], &MPU6050_data[2], &MPU6050_data[3], &MPU6050_data[4], &MPU6050_data[5]);            //(&ax, &ay, &az, &gx, &gy, &gz);
			if((sensor_flag1 == sensor_flag2) && (sensor_flag1 & 0x80))
			{
				MPU.value = MPU6050_data[0];
				TX_buf_sensor[4] = MPU.buff[1];
				TX_buf_sensor[5] = MPU.buff[0];
			}
			if((sensor_flag1 == sensor_flag2) && (sensor_flag1 & 0x40))
			{
				MPU.value = MPU6050_data[1];
				TX_buf_sensor[6] = MPU.buff[1];
				TX_buf_sensor[7] = MPU.buff[0];
			}
			if((sensor_flag1 == sensor_flag2) && (sensor_flag1 & 0x20))
			{
				MPU.value = MPU6050_data[2];
				TX_buf_sensor[8] = MPU.buff[1];
				TX_buf_sensor[9] = MPU.buff[0];
			}
			if((sensor_flag1 == sensor_flag2) && (sensor_flag1 & 0x10))
			{
				MPU.value = MPU6050_data[3];
				TX_buf_sensor[10] = MPU.buff[1];
				TX_buf_sensor[11] = MPU.buff[0];
			}
			if((sensor_flag1 == sensor_flag2) && (sensor_flag1 & 0x08))
			{
				MPU.value = MPU6050_data[4];
				TX_buf_sensor[12] = MPU.buff[1];
				TX_buf_sensor[13] = MPU.buff[0];
			}
			if((sensor_flag1 == sensor_flag2) && (sensor_flag1 & 0x04))
			{
				MPU.value = MPU6050_data[5];
				TX_buf_sensor[14] = MPU.buff[1];
				TX_buf_sensor[15] = MPU.buff[0];
			}
		}
		if((sensor_flag1 == sensor_flag2) && (sensor_flag1 & 0x02))
		{
			Encoder_value_L = ENCODER_CNT_L;
			Encoder_value_R = ENCODER_CNT_R;
			TX_buf_sensor[16] = Encoder_value_L >> 8;
			TX_buf_sensor[17] = Encoder_value_L & 0xff;
			TX_buf_sensor[18] = Encoder_value_R >> 8;
			TX_buf_sensor[19] = Encoder_value_R & 0xff;
		}
		if((sensor_flag1 == sensor_flag2) && (sensor_flag1 & 0x01))
		{
			TX_buf_sensor[20] = 0;
		}
		if(sensor_flag1 == sensor_flag2)
		{
			TX_buf_sensor[21] = TX_buf_sensor[2];
			for(z=4;z<21;z++)
				TX_buf_sensor[21] += TX_buf_sensor[z];
			Serial.write(TX_buf_sensor,22);
		}
	}
}

void serialEvent()
{
	unsigned char z,tmp=0;
	Serial.readBytes(RX_buf,7);
	if((RX_buf[0] == 0x76) && (RX_buf[1] == 0))
	{
		DEBUG_PRINT("\n\r RX_data : ");
		for(z=0;z<7;z++)
		{
			DEBUG_PRINTF(RX_buf[z],HEX);
			DEBUG_PRINT(" ");
		}
		for(z=2;z<6;z++)
			tmp += (unsigned char)RX_buf[z];
		tmp &= 0xFF;
		if((unsigned char)RX_buf[6] == tmp)
		{
			switch(RX_buf[2])
			{
				case 0x20:
					switch(RX_buf[4])
					{
						case FORWARD:
						case BACKWARD:
							Motor_Control('A',RX_buf[5]*40+95);
							break;
						case LEFT_U:
						case LEFT_B:
						case LEFT:
						case LIGHT_U:
						case LIGHT_B:
						case LIGHT:
							Motor_Control('A',RX_buf[5]*25+155);
							break;
						default:
							Motor_Control('A',0);
							break;
					}
					Motor_mode(RX_buf[4]);
					TX_buf[6] = TX_buf[2];
					for(z=4;z<6;z++)
					{
						TX_buf[z] = (unsigned char)RX_buf[z];
						TX_buf[6] += TX_buf[z];
					}
					//Serial.write(TX_buf,7);
					DEBUG_PRINT("\n\r TX_data : ");
					for(z=0;z<7;z++)
					{
						Serial.write(TX_buf[z]);
						DEBUG_PRINTF(TX_buf[z],HEX);
						DEBUG_PRINT(" ");
						delay(5);
					}
					break;
				case 0x30:
					sensor_flag2 = ((unsigned int)(RX_buf[4]<<8) | (unsigned char)RX_buf[5]);
					DEBUG_PRINTLN(sensor_flag2);
					break;
				case 0xF0:
					delay(500);
					asm("jmp 0");
					break;
			}
		}
	}
}

void serialEvent1()
{
	unsigned char z,tmp=0;
	Serial1.readBytes(RX_ultra,17);
	if((RX_ultra[0] == 0x76) && (RX_ultra[1] == 0))
	{
		for(z=2;z<16;z++)
			tmp += (unsigned char)RX_ultra[z];
		tmp = tmp & 0xFF;
		if((unsigned char)RX_ultra[16] == tmp)
		{
			TX_buf_ultra[16] = TX_buf_ultra[2];
			for(z=4;z<16;z++)
			{
				TX_buf_ultra[z] = (unsigned char)RX_ultra[z];
				TX_buf_ultra[16] += TX_buf_ultra[z];
			}
			if(Ultra_EN)
				Serial.write(TX_buf_ultra,17);
		}
	}
	else
	{
		for(z=1;z<17;z++)
		{
			if(RX_ultra[z]==0x76)
			{
				if(z!=16)
				{
					if(RX_ultra[z+1]==0)
						tmp = z;
				}
				else
				{
					tmp = z;
				}
			}
		}
		Serial1.readBytes(RX_ultra,tmp);
	}
}

void Motor_mode(int da)
{
	int z;
	for(z=0;z<4;z++)
		digitalWrite(Motor[z],(da>>z) & 0x01);
}

void Motor_Control(char da, unsigned int OC_value)
{
	switch(da)
	{
		case 'L':
			analogWrite(Motor[4],OC_value);
			break;
		case 'R':
			analogWrite(Motor[5],OC_value);
			break;
		case 'A':
			analogWrite(Motor[4],OC_value);
			analogWrite(Motor[5],OC_value);
			break;
	}
}

void Encoder_count_L()
{
	ENCODER_CNT_L++;
}

void Encoder_count_R()
{
	ENCODER_CNT_R++;
}

void Timer1_ISR()
{
	Timer_flag = 1;
}
