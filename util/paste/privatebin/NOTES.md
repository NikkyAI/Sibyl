## privatebin TODO

https://github.com/PrivateBin/PrivateBin/wiki/API

https://github.com/PrivateBin/PrivateBin/wiki/Encryption-format

```
{
	"v":2,
	"adata":[
		[
			"base64 encoded iv", // base64(cipher_iv)
			"base64 encoded salt", // base64(kdf_salt)
			100000, // kdf_iterations
			256, // kdf_keysize
			128, // cipher_tag_size
			"aes", // cipher_algo
			"gcm", // cipher_mode
			"zlib" // compression type - "zlib" or "none" 
//(the rawdeflate library used before PrivateBin version 1.3 is not quite zlib compatible)
		],
		"plaintext", // format of the paste - "plaintext" or "syntaxhighlighting" or "markdown",
		0, // open discussion flag 1 or 0
		0 // burn after reading flag 1 or 0
	],
	"ct": "base64 of cipher text",
	"meta": {
		"expire":"1week"
	}
}
```

## key derivation PBKDF2

```
kdf_salt = random(8) # 8 bytes
kdf_iterations = 100000 # was 10000 before PrivateBin version 1.3
kdf_keysize = 256 # bits of resulting kdf_key

kdf_key = PBKDF2_HMAC_SHA256(kdf_keysize, kdf_salt, paste_password)
```

### encryption
```
cipher_algo = "aes"
cipher_mode = "gcm" # was "ccm" before PrivateBin version 1.0
cipher_iv = random(16) # 128 bit
cipher_tag_size = 128

cipher_text = cipher(AES(kdf_key), GCM(iv, paste_meta), paste_blob)
```

[https://github.com/r4sas/PBinCLI/blob/master/pbincli/format.py#L261-L295](https://github.com/r4sas/PBinCLI/blob/master/pbincli/format.py#L261-L295)

https://medium.com/@rrohaill/aes-gcm-encryption-decryption-using-kotlin-7f08884eb15b
https://github.com/rrohaill/Cryptography



expires:
```
5min
10min
1hour
1day
1week
1month
1year
never
```

https://github.com/r4sas/PBinCLI/blob/94023a986d87769a230250e1ce39c2c93427b6a9/pbincli/format.py#L140
```python
    def _encryptV2(self):
        from pbincli.utils import json_encode

        iv = get_random_bytes(int(self._tag_bits / 8))
        salt = get_random_bytes(self._salt_bytes)
        key = self.__deriveKey(salt)

        # prepare encryption authenticated data and message
        adata = [
            [
                b64encode(iv).decode(),
                b64encode(salt).decode(),
                self._iteration_count,
                self._block_bits,
                self._tag_bits,
                'aes',
                'gcm',
                self._compression
            ],
            self._formatter,
            int(self._discussion),
            int(self._burnafterreading)
        ]
        cipher_message = {'paste':self._text}
        if self._attachment:
            cipher_message['attachment'] = self._attachment
            cipher_message['attachment_name'] = self._attachment_name

        cipher = self.__initializeCipher(key, iv, adata, int(self._tag_bits /8 ))
        ciphertext, tag = cipher.encrypt_and_digest(self.__compress(json_encode(cipher_message)))

        if self._debug: print("PBKDF2 Key:\t{}\nCipherText:\t{}\nCipherTag:\t{}"
            .format(b64encode(key), b64encode(ciphertext), b64encode(tag)))

        self._data = {'v':2,'adata':adata,'ct':b64encode(ciphertext + tag).decode(),'meta':{'expire':self._expiration}}

    def json_encode(s):
        return json.dumps(s, separators=(',',':')).encode()

    @classmethod
    def __initializeCipher(self, key, iv, adata, tagsize):
        from pbincli.utils import json_encode

        cipher = AES.new(key, AES.MODE_GCM, nonce=iv, mac_len=tagsize)
        cipher.update(json_encode(adata))
        return cipher


    def __compress(self, s):
        if self._version == 2 and self._compression == 'zlib':
            # using compressobj as compress doesn't let us specify wbits
            # needed to get the raw stream without headers
            co = zlib.compressobj(wbits=-zlib.MAX_WBITS)
            return co.compress(s) + co.flush()
        elif self._version == 2 and self._compression == 'none':
            # nothing to do, just return original data
            return s
        elif self._version == 1:
            co = zlib.compressobj(wbits=-zlib.MAX_WBITS)
            b = co.compress(s) + co.flush()
            return b64encode(''.join(map(chr, b)).encode('utf-8'))
        else:
            PBinCLIError('Unknown compression type provided!')
```

```
D:\dev\Sibyl>pbincli send --text text -c none --debug
Set paste version to 2
PBKDF2 Key:     b'lB4WjbmrcSwzXSW8pl0UvJDJJWHbb9YTiJmLDH0V6Vc='
CipherText:     b'8d25b5hfpEqjQBRT5LoCOQ=='
CipherTag:      b'9bO+VSQWdw8oxtpQCME87Q=='
Passphrase:     CdD6aMAR5P4DKsSPF8QwY6gxmZkgbQpgfwRzNZC5tGun
Request:        {"v":2,"adata":[["Nj0n5nF//Mq8PfVJcpTeUw==","Ah0vI217WKA=",100000,256,128,"aes","gcm","none"],"plaintext",0,0],"ct":"8d25b5hfpEqjQBRT
5LoCOfWzvlUkFncPKMbaUAjBPO0=","meta":{"expire":"1day"}}
Response:       {'status': 0, 'id': 'd8bff13c62ab0685', 'url': '/?d8bff13c62ab0685', 'deletetoken': 'de72828dceac032e2acf353655132910bc33131c0371ad3d3043db4e165b7bb8'}

Paste uploaded!
PasteID:        d8bff13c62ab0685
Password:       CdD6aMAR5P4DKsSPF8QwY6gxmZkgbQpgfwRzNZC5tGun
Delete token:   de72828dceac032e2acf353655132910bc33131c0371ad3d3043db4e165b7bb8

Link:           https://paste.i2pd.xyz/?d8bff13c62ab0685#CdD6aMAR5P4DKsSPF8QwY6gxmZkgbQpgfwRzNZC5tGun

D:\dev\Sibyl>
```